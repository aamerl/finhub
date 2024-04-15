def get_schema_registry():
    with open("trade.avsc") as f:
        schema_str = f.read()
    return schema_str
