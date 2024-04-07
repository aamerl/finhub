#!/usr/bin/env python
# -*- coding: utf-8 -*-

from confluent_kafka import Consumer
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer

if __name__ == "__main__":

    sr_conf = {'url': "http://schema-registry:8081"}
    schema_registry_client = SchemaRegistryClient(sr_conf)
    schema = schema_registry_client.get_latest_version("schema_test_1")
    print(schema.schema_id)
    print(schema.version)

    avro_deserializer = AvroDeserializer(schema_registry_client, schema.schema)

    consumer_conf = {'bootstrap.servers': "kafka:9092",
                     'group.id': "test_1",
                     'auto.offset.reset': "earliest"}

    consumer = Consumer(consumer_conf)
    consumer.subscribe(["test_topic"])

    while True:
        try:
            # SIGINT can't be handled when polling, limit timeout to 1 second.
            msg = consumer.poll(1.0)
            if msg is None:
                continue

            data = avro_deserializer(msg.value(), SerializationContext(msg.topic(), MessageField.VALUE))
            if data is not None:
                print(f"data: {data}")
        except KeyboardInterrupt:
            break

    consumer.close()
