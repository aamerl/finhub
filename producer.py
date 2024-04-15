import json
import logging

import websocket
from confluent_kafka import Producer
from confluent_kafka.schema_registry import Schema, SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import MessageField, SerializationContext


def send_message_to_kafka(data):
    schema_registry_url = "http://schema-registry:8081"
    with open("trade.avsc") as f:
        schema_str = f.read()

    schema_registry_conf = {"url": schema_registry_url}
    schema_registry_client = SchemaRegistryClient(schema_registry_conf)
    avro_serializer = AvroSerializer(schema_registry_client, schema_str)
    schema_id = schema_registry_client.register_schema(
        "schema_test_1", Schema(schema_str, "AVRO")
    )
    print(schema_id)

    producer_config = {"bootstrap.servers": "kafka:9092"}
    producer = Producer(producer_config)
    producer.produce(
        topic="test1",
        value=avro_serializer(data, SerializationContext("test1", MessageField.VALUE)),
    )
    producer.flush()


def on_message(ws, message):
    message = json.loads(message)
    data = message.get("data")
    message_type = message.get("type")

    kafka_message = {"data": data, "type": message_type}

    send_message_to_kafka(kafka_message)


def on_error(ws, error):
    print(f"WebSocket Error: {error}")


def on_close(ws, a, b):
    print("WebSocket connection closed")


def on_open(ws):
    symbols = ["BINANCE:BTCUSDT", "AAPL"]
    for symbol in symbols:
        subscription_message = {"type": "subscribe", "symbol": symbol}
        ws.send(json.dumps(subscription_message))


if __name__ == "__main__":
    # websocket.enableTrace(True)
    logging.basicConfig(level=logging.DEBUG)
    KEY = "co3kaq9r01qj6vn838b0co3kaq9r01qj6vn838bg"
    ws = websocket.WebSocketApp(
        f"wss://ws.finnhub.io?token={KEY}",
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )
    ws.on_open = on_open
    ws.run_forever()
