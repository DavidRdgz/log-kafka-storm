package com.dvidr

import java.io.File
import java.util.Properties
import kafka.javaapi.producer.Producer
import kafka.producer.{KeyedMessage,ProducerConfig}
import org.apache.commons.io.input.{TailerListenerAdapter, Tailer, TailerListener}



case class MyKafkaProducer(bootstrapServers: String, topic: String, aFile: String) {

  @throws(classOf[InterruptedException])
  def run {
    val listener: TailerListener = new MyListener()
    val tailer: Tailer = new Tailer(new File(aFile), listener, MyKafkaProducer.SLEEP)
    tailer.run
  }

  class MyListener extends TailerListenerAdapter {
    private val props = setProducerProps(bootstrapServers)
    private val config =  new ProducerConfig(props)
    private val producer = new Producer[String, String](config)

    def setProducerProps(bServers: String): Properties = {
      val props = new Properties()
      props.put("metadata.broker.list", bServers)
      props.put("serializer.class", "kafka.serializer.StringEncoder")
      props.put("request.required.acks", "1")
      return props
    }

    override def handle(line: String) {
      println(line)
      val data = new KeyedMessage[String, String](topic, line)
      producer.send(data)
    }
  }
}

object MyKafkaProducer {
  private val SLEEP: Int = 1

  @throws(classOf[Exception])
  def main(args: Array[String]) {
    if (args.length != 3) {
      System.err.println("Usage: MyKafkaProducer <bootstrap.servers> <topic> <log.file>")
      System.err.println("Example: MyKafkaProducer \"localhost:9092\" \"sys_logs\" \"/var/log/system.log\"")
      System.exit(1)
    }

    val Array(bootStrapServers, topic, aFile) = args
    //val Array(bootStrapServers, topic, aFile) = Array("localhost:9092", "sys_logs", "/var/log/system.log")
    val app = MyKafkaProducer(bootStrapServers, topic, aFile)
    app.run
  }
}