name := "spark_job"

organization := "org.finhub"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12"

ThisBuild / javaVersion := "17"

// Repositories
resolvers += "Confluent" at "https://packages.confluent.io/maven/"

// Dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "org.apache.spark" %% "spark-avro" % "3.5.1",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1",
  "za.co.absa" %% "abris" % "6.4.0",
  "log4j" % "log4j" % "1.2.17",
  "junit" % "junit" % "4.13.2" % Test,
  "com.datastax.spark" %% "spark-cassandra-connector" % "3.5.0",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.3"
)

// Packaging as a fat JAR (shading)
assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("META-INF/*.SF" -> "META-INF/SF.excluded")
    .inAll
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

mainClass in assembly := Some("org.finhub.spark_job.SparkJob")
