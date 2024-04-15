#!/bin/bash

# Wait for Kafka to start
echo "Waiting for Kafka to start..."
sleep 5

# Create Kafka topics
echo "Creating Kafka topics..."
kafka-topics.sh --create -bootstrap-server kafka-0:9092 --partitions 2 --replication-factor 2 --if-not-exists --topic test1
kafka-topics.sh --create -bootstrap-server kafka-0:9092 --partitions 2 --replication-factor 2 --if-not-exists --topic trade1
# Add more topic creation commands as needed

echo "Kafka topics created successfully."
