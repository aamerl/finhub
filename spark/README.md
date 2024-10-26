docker exec -it spark-master bash

jar tf /opt/spark-apps/app.jar
/opt/spark/bin/spark-submit --master spark://spark-master:7077 --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.spark:spark-avro_2.13:3.5.1 --class org.finhub.kafka_to_cassandra.KafkaToCassandra /opt/spark-apps/app.jar


/opt/spark/bin/spark-submit --master spark://spark-master:7077 --class org.finhub.kafka_to_cassandra.KafkaToCassandra /opt/spark-apps/app.jar

/opt/spark/bin/spark-submit --class org.finhub.kafka_to_cassandra.KafkaToCassandra /opt/spark-apps/app.jar

docker build -t sp-test - && docker run --rm sp-test


docker build -t cluster-apache-spark:3.5.1 .
ssh-keygen -t ed25519 -C "lhoussaine.aamer@outlook.fr"
aamerlh