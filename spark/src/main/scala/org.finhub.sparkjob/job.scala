package org.finhub.sparkjob

import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.streaming.{StreamingQuery, StreamingQueryException}
import org.apache.spark.sql.functions._
import za.co.absa.abris.avro.functions
import za.co.absa.abris.config.AbrisConfig
import za.co.absa.abris.config.FromAvroConfig
import java.util.concurrent.TimeoutException
import java.util.logging.Logger

object SparkJob {
  val APP_NAME = "SparkJob"
  val KAFKA_SOURCE_PROVIDER =
    "org.apache.spark.sql.kafka010.KafkaSourceProvider"
  val CASSANDRA_SOURCE_PROVIDER = "org.apache.spark.sql.cassandra"
  val KAFKA_OFFSET_METHOD = "earliest"
  val sparkjob_CLASS = "--class org.finhub.sparkjob.SparkJob"
  val kafkaBootstrapServers = sys.env("KAFKA_BOOTSTRAP_SERVERS")
  val sparkMaster = sys.env("SPARK_MASTER")
  val cassandraHost = sys.env("CASSANDRA_HOST")
  val schemaRegistryUrl = sys.env("SCHEMA_REGISTRY_URL")
  val kafkaTopic = sys.env("KAFKA_TOPIC")
  val cassandraKeyspace = sys.env("CASSANDRA_KEYSPACE")
  val cassandraTradesTables = sys.env("CASSANDRA_TRADE_TABLE")
  val cassandraSummaryTable = sys.env("CASSANDRA_SUMMARY_TABLE")

  val logger: Logger = Logger.getLogger(APP_NAME)

  def main(args: Array[String]): Unit = {
    val kafkaConsumerProperties = Map(
      "kafka.bootstrap.servers" -> kafkaBootstrapServers,
      "startingOffsets" -> KAFKA_OFFSET_METHOD,
      "subscribe" -> kafkaTopic
    )

    val abrisConfig: FromAvroConfig =
      AbrisConfig.fromConfluentAvro.downloadReaderSchemaByLatestVersion
        .andTopicNameStrategy(kafkaTopic, false)
        .usingSchemaRegistry(schemaRegistryUrl)

    start(kafkaConsumerProperties, abrisConfig)
  }

  /*
   * The processing code.
   */
  private def start(
      kafkaConsumerProperties: Map[String, String],
      abrisConfig: FromAvroConfig
  ): Unit = {
    // Create SparkSession
    val spark: SparkSession = SparkSession
      .builder()
      .appName(APP_NAME)
      .config("spark.cassandra.connection.host", cassandraHost)
      .config("spark.master", sparkMaster)
      .config("spark.class", sparkjob_CLASS)
      .getOrCreate()

    val tradeDf: Dataset[Row] = spark.readStream
      .format(KAFKA_SOURCE_PROVIDER)
      .options(kafkaConsumerProperties)
      .load()

    // Define the Cassandra write operation within foreachBatch
    val expandedDF: Dataset[Row] = tradeDf
      .withColumn("avroData", functions.from_avro(col("value"), abrisConfig))
      .select(col("avroData.*"))
      .select(explode(col("data")), col("type"))
      .select(col("col.*"))

    val finalDF: Dataset[Row] = getFinalDf(expandedDF)

    // Write query to Cassandra for trade data
    val finalQuery: StreamingQuery = finalDF.writeStream
      .foreachBatch((batchDF: Dataset[Row], batchID: Long) => {
        logger.info(s"Writing to Cassandra batch $batchID")
        batchDF.write
          .format(CASSANDRA_SOURCE_PROVIDER)
          .option("keyspace", cassandraKeyspace)
          .option("table", cassandraTradesTables)
          .mode("append")
          .save()
      })
      .outputMode("update")
      .start()

    val finalSummaryDF: Dataset[Row] = getSummaryDf(finalDF)

    // Write second query to Cassandra for summary data
    val summaryQuery: StreamingQuery = finalSummaryDF.writeStream
      .foreachBatch((batchDF: Dataset[Row], batchID: Long) => {
        logger.info(s"Writing to Cassandra batch $batchID")
        batchDF.write
          .format(CASSANDRA_SOURCE_PROVIDER)
          .option("keyspace", cassandraKeyspace)
          .option("table", cassandraSummaryTable)
          .mode("append")
          .save()
      })
      .outputMode("update")
      .start()

    finalQuery.awaitTermination()
    summaryQuery.awaitTermination()
  }

  private def getFinalDf(expandedDF: Dataset[Row]): Dataset[Row] = {
    // Rename columns and add proper timestamps
    expandedDF
      .withColumnRenamed("c", "trade_conditions")
      .withColumnRenamed("p", "price")
      .withColumnRenamed("s", "symbol")
      .withColumnRenamed("t", "trade_timestamp")
      .withColumnRenamed("v", "volume")
      .withColumn(
        "trade_timestamp",
        to_timestamp(expr("trade_timestamp / 1000"))
      )
      .withColumn(
        "ingest_timestamp",
        current_timestamp().as("ingest_timestamp")
      )
  }

  private def getSummaryDf(finalDF: Dataset[Row]): Dataset[Row] = {
    // Create another dataframe with aggregates - running averages from the last 15 seconds
    val summaryDF: Dataset[Row] = finalDF
      .withColumn("price_volume_multiply", expr("price * volume"))
      .withWatermark("trade_timestamp", "15 seconds")
      .groupBy("symbol")
      .agg(avg("price_volume_multiply"))

    // Rename columns in dataframe and add UUIDs before inserting to Cassandra
    summaryDF
      .withColumn(
        "ingest_timestamp",
        current_timestamp().as("ingest_timestamp")
      )
      .withColumnRenamed("avg(price_volume_multiply)", "price_volume_multiply")
  }
}
