import json
import logging

import websocket
from confluent_kafka import Producer
from confluent_kafka.schema_registry import Schema, SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import MessageField, SerializationContext

import settings as settings
from utils import get_avro_schema


def send_message_to_kafka(data):
    schema_str = get_avro_schema()
    schema_registry_client = SchemaRegistryClient({"url": settings.SCHEMA_REGISTRY_URL})
    avro_serializer = AvroSerializer(schema_registry_client, schema_str)

    if settings.FIRST_RUN:
        schema_id = schema_registry_client.register_schema(
            settings.SCHEMA_SUBJECT_NAME, Schema(schema_str, "AVRO")
        )
        logging.info(f"trade schema sent to schema registry with id  {schema_id}")

    producer_config = {
        "bootstrap.servers": settings.KAFKA_URL,
        "batch.num.messages": 1,
        "queue.buffering.max.messages": 1,
        "message.send.max.retries": 3,
        "message.timeout.ms": 5000,
    }
    producer = Producer(producer_config)
    producer.produce(
        topic=settings.TOPIC_KAFKA,
        value=avro_serializer(
            data, SerializationContext(settings.TOPIC_KAFKA, MessageField.VALUE)
        ),
    )
    producer.flush()
    logging.info("data sent to Kafka")


def on_message(ws, message):
    message = json.loads(message)
    data = message.get("data")
    message_type = message.get("type")
    kafka_message = {"data": data, "type": message_type}
    send_message_to_kafka(kafka_message)


def on_error(ws, error):
    logging.error(f"WebSocket Error: {error}")


def on_close(ws, a, b):
    logging.info("WebSocket connection closed")


def on_open(ws):
    for symbol in settings.FINHUB_SYMBOLS.split(","):
        subscription_message = {"type": "subscribe", "symbol": symbol}
        ws.send(json.dumps(subscription_message))


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    if settings.DEBUG:
        websocket.enableTrace(True)
        logging.basicConfig(level=logging.DEBUG)

    ws = websocket.WebSocketApp(
        f"{settings.FINHUB_API_URL}{settings.FINHUB_SECRET_KEY}",
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )
    ws.on_open = on_open
    ws.run_forever()
