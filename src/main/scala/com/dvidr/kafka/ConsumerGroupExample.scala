package com.dvidr

import collection.JavaConversions._
import kafka.consumer.ConsumerConfig
import kafka.consumer.Consumer
import kafka.consumer.ConsumerIterator
import kafka.consumer.KafkaStream
import java.util.Properties
import java.util.concurrent.{TimeUnit, ExecutorService, Executors}


case class ConsumerGroupExample(a_zookeeper: String, a_topic: String, a_groupId: String) {
  val consumer = Consumer.create(createConsumerConfig(a_zookeeper, a_groupId))
  var executor: ExecutorService = null

  def shutdown() {
    if (consumer != null) consumer.shutdown()
    if (executor != null) executor.shutdown()
    try {
      if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
        println("Timed out waiting for consumer threads to shut down, exiting uncleanly")
      }
    } catch {
      case e: InterruptedException => println("Interrupted during shutdown, exiting uncleanly")
    }
  }

  def run(numThreads: Int) = {
    val topicCountMap = Map(a_topic -> numThreads)
    val consumerMap = consumer.createMessageStreams(topicCountMap)
    val streams = consumerMap.get(a_topic).get

    executor = Executors.newFixedThreadPool(numThreads)
    var threadNumber = 0
    for (stream <- streams) {
      executor.submit(new ConsumerTest(stream, threadNumber))
      threadNumber += 1
    }
  }

  def createConsumerConfig(a_zookeeper: String, a_groupId: String): ConsumerConfig = {
    val props = new Properties()
    props.put("zookeeper.connect", a_zookeeper)
    props.put("group.id", a_groupId)
    props.put("zookeeper.session.timeout.ms", "400")
    props.put("zookeeper.sync.time.ms", "200")
    props.put("auto.commit.interval.ms", "1000")

    return new ConsumerConfig(props)
  }
}

object ConsumerGroupExample {

  @throws(classOf[Exception])
  def main(args: Array[String]) {
    if (args.length != 3) {
      System.err.println("Usage: MyKafkaProducer <bootstrap.servers> <topic> <group.id> <threads>")
      System.err.println("Example: MyKafkaProducer \"localhost:2181\" \"sys_logs\" \"group1\" \"1\"")
      System.exit(1)
    }

    val Array(zooKeeper, topic, groupId, threads) = args
    //val Array(zooKeeper, topic, groupId, threads) = Array("localhost:2181", "sys_logs", "group1", "1")

    val example = new ConsumerGroupExample(zooKeeper, topic, groupId)
    example.run(threads.toInt)

    try {
      Thread.sleep(1000000)
    } catch {
      case ie: InterruptedException => println("Interrupted during run threads, exiting uncleanly")
    }
    example.shutdown()
  }
}

case class ConsumerTest(m_stream: KafkaStream[Array[Byte], Array[Byte]], m_threadNumber: Int) extends Runnable {
  def run() = {
    val it: ConsumerIterator[Array[Byte], Array[Byte]] = m_stream.iterator()
    while (it.hasNext())
      println("Thread " + m_threadNumber + ": " + new String(it.next().message()))
      println("Shutting down Thread: " + m_threadNumber)
  }
}

