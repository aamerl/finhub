import os

SCHEMA_REGISTRY_URL = os.getenv("SCHEMA_REGISTRY_URL", "http://schema-registry:8081")
SCHEMA_SUBJECT_NAME = os.getenv("SCHEMA_SUBJECT_NAME", "trade_schema")
KAFKA_URL = os.getenv("KAFKA_URL", "kafka-0:9092,kafka-1:9092")
TOPIC_KAFKA = os.getenv("TOPIC_KAFKA", "trade1")
FINHUB_SYMBOLS = os.getenv("FINHUB_SYMBOLS", "BINANCE:BTCUSDT,AAPL")
FINHUB_SECRET_KEY = os.getenv("FINHUB_SECRET_KEY", "")
FINHUB_API_URL = os.getenv("FINHUB_API_URL", "wss://ws.finnhub.io?token=")
FIRST_RUN = os.getenv("FIRST_RUN", "true").lower() in ["true", "yes"]
FINHUB_TRACE = os.getenv("FINHUB_TRACE", "false").lower() in ["true", "yes"]
