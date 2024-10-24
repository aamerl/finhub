def get_avro_schema() -> str:
    """read trade message schema

    Returns:
        str: the avro schema of messages
    """
    with open("trade.avsc") as f:
        schema_str = f.read()
    return schema_str
