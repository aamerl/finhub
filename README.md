
# Financial Data Pipeline Project

This project implements a data processing pipeline that retrieves real-time financial data from the Finhub API, stores it in Kafka, processes it with Apache Spark, and stores it in Cassandra for long-term storage. The processed data is then visualized in Grafana.

## Prerequisites

- **Docker** and **Docker Compose** must be installed.
- Finhub API key (configured in the `.env` file).
- Configured volumes for Kafka and Cassandra.

## Configuration

1. **.env File**: This file contains environment variables for configuration, including the Finhub API key.
   
2. **Docker Compose**: The `docker-compose.yml` file defines all required services, including Kafka, Spark, Cassandra, and Grafana.

## Installation and Execution Steps

### 1. Build the Spark Docker Image

Before launching the cluster, build the Spark image:

```bash
docker build -t cluster-apache-spark:3.5.1 .
```

### 2. Launch Docker Compose

Start all services in detached mode:

```bash
docker-compose up -d
```

### 3. Initialize Services

Run the following commands in order to configure Kafka, Cassandra, Spark, and the data producer.

#### Step 1: Configure Kafka (create topics)

```bash
docker exec -it kafka-0 bash ./init-kafka.sh
```
![alt text](/docs/image-1.png)

#### Step 2: Initialize Cassandra (create tables)

```bash
docker exec -it cassandra bash ./init-cassandra.sh
```

#### Step 3: Launch the Spark Streaming Job

```bash
docker exec -it spark-master bash /opt/spark/bin/spark-submit --class org.finhub.sparkjob.SparkJob /opt/spark-apps/app.jar
```
![alt text](/docs/image-2.png)

#### Step 4: Start the Python Data Producer

```bash
docker exec -it producer python producer.py
```
![alt text](/docs/image-3.png)

## Services Structure

### `producer`

- Retrieves financial data from the Finhub API and publishes it to a Kafka topic.
- **Environment Variables**:
  - `SCHEMA_REGISTRY_URL`
  - `KAFKA_URL`
  - `TOPIC_KAFKA`
  - `FINHUB_SYMBOLS`
  - `FINHUB_SECRET_KEY`
  - `FINHUB_API_URL`
  - `FIRST_RUN`

### `kafka-0` and `kafka-1`

- Kafka cluster using Bitnami for message streaming.
- Configures controller and broker roles for redundancy.

### `schema-registry`

- Provides a schema registry for Kafka.
- Manages the format of exchanged data.

### `spark-master` and `spark-worker-0`

- **Spark Streaming** processes data in real time from Kafka.
- **Spark Job** computes and stores results in Cassandra.

### `cassandra`

- NoSQL database for storing processed data.
- The `trades` and `running_averages_15_sec` tables are created via `cassandra-setup.cql`.

### `grafana`

- Data visualization tool to monitor real-time analytics.
- Connected to Cassandra to extract and display processed financial data.

## Grafana Visualization

1. Access Grafana at `localhost:3000`.
2. Configure dashboards to visualize data from the `trades` table in Cassandra.
3. Use the following settings:
   - **Time Column**: `ingest_timestamp`
   - **Value Column**: `price`
   - **ID Column**: `symbol`
![alt text](/docs/image-4.png)

## Monitoring Performance

To monitor container resource usage in real-time, use:

```bash
docker stats
```
This command will show the performance and resource usage of each container.
![alt text](/docs/image.png)

## Local Development

The `devcontainer` folder allows for local development and testing of the `producer` service by isolating it in a development container.

---

## Data Structure Example

### Cassandra (trades)

```cql
CREATE TABLE IF NOT EXISTS market.trades (
    symbol text,
    trade_conditions text,
    price double,
    volume double,
    trade_timestamp timestamp,
    ingest_timestamp timestamp,
    PRIMARY KEY((symbol), trade_timestamp)
) WITH CLUSTERING ORDER BY (trade_timestamp DESC);
```

### Kafka Schema File (trade.avsc)

This JSON file defines the schema for data produced by `producer`.

---

## Author and License

Project by Lhoussaine Aamer (lhoussaine.aamer@outlook.fr), licensed under MIT.

---

This guide will help you configure and deploy the full data pipeline. For additional questions, please consult the documentation for Docker, Kafka, Spark, Cassandra, or contact the development team.