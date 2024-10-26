name := "sparkjob"

organization := "org.finhub"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.19"

javacOptions ++= Seq("--release", "17")

// Repositories
resolvers += "Confluent" at "https://packages.confluent.io/maven/"

// Dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "org.apache.spark" %% "spark-avro" % "3.5.1",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1",
  "za.co.absa" %% "abris" % "6.4.1",
  "log4j" % "log4j" % "1.2.17",
  "junit" % "junit" % "4.13.2" % Test,
  "com.datastax.spark" %% "spark-cassandra-connector" % "3.5.0",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.3",
  "com.typesafe" % "config" % "1.4.1"
)

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF/*.SF")  => MergeStrategy.discard
  case PathList("META-INF/*.DSA") => MergeStrategy.discard
  case PathList("META-INF/*.RSA") => MergeStrategy.discard
  case _                          => MergeStrategy.first
}

lazy val app = (project in file("app"))
  .settings(
    assembly / mainClass := Some("org.finhub.sparkjob.SparkJob"),
    assembly / assemblyJarName := "sparkjob.jar"
  )
