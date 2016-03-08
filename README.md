# log-storm-kafka
Quick setup to parse logs using Kafka and Storm with Algebird's probabilistic data structures powering the analytics.

# Quickstart

## Zookeeper & Kafka Server Start
Download Kafka and start the zookeeeper and kafka servers from within the Kafka folder.

```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

And then,

```
bin/kafka-server-start.sh config/server.properties
```

lastly,

```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic sys_logs
```

You should now have a Kafka topic `sys_logs` which a Kafka producer sends to and Kafka consumer reads from.

## Kafka Log Producer Start

Next, clone this repo:

```
git clone https://github.com/DavidRdgz/log-kafka-storm.git
```

Next `cd` 

```
cd log-kafka-storm/target/
```

Then run,

```
java -cp .:./kafka-storm-1.0-SNAPSHOT-jar-with-dependencies.jar com.dvidr.MyKafkaProducer localhost:9092 sys_logs /var/log/system.log
```

your logs should be tailed and send from a Kafka producer.

## Storm Topology

Lastly, we run the Storm topology of choice

```
java -cp .:./kafka-storm-1.0-SNAPSHOT-jar-with-dependencies.jar com.dvidr.UniqueTopicCountTopology 
```

this parses the logs in a stream maintaining a the unique number of log codes with fixed amount of memory using Algebird's HyperLogLog implementation.
