SCHEMA_AVRO = """
{
  "type" : "record",
  "name" : "message",
  "namespace" : "FinnhubProducer",
  "fields" : [ {
    "name" : "data",
    "type" : {
      "type" : "array",
      "items" : {
        "type" : "record",
        "name" : "data",
        "fields" : [ {
          "name" : "c",
          "type":[
            {
               "type":"array",
               "items":["null","string"],
               "default":[]
            },
            "null"
          ],
          "doc" : "Trade conditions"
        }, 
        {
          "name" : "p",
          "type" : "double",
          "doc" : "Price at which the stock was traded"
        }, 
        {
          "name" : "s",
          "type" : "string",
          "doc" : "Symbol of a stock"
        }, 
        {
          "name" : "t",
          "type" : "long",
          "doc" : "Timestamp at which the stock was traded"
        }, 
        {
          "name" : "v",
          "type" : "double",
          "doc" : "Volume at which the stock was traded"
        } ]
      },
      "doc" : "Trades messages"
    },
    "doc"  : "Contains data inside a message"
  }, 
  {
    "name" : "type",
    "type" : "string",
    "doc"  : "Type of message"
  } ],
  "doc" : "A schema for upcoming Finnhub messages"
}
"""