#!/bin/bash

# Wait for Kafka to start
echo "Python producer to Kafka will start in 1s"
sleep 1

python producer.py
