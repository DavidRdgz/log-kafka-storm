package com.dvidr.storm.bolt

import java.io.UnsupportedEncodingException
import backtype.storm.task.OutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.Fields
import backtype.storm.tuple.Tuple
import backtype.storm.tuple.Values
import java.util.{Map => JMap}
//import scala.util.matching.Regex

class SplitSentenceBolt extends BaseRichBolt {
  var collector: OutputCollector = _

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    this.collector = collector
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("word"))
  }

  override def execute(tuple: Tuple) {
    val value = tuple.getValue(0)
    var sentence = ""
    //val pattern = new Regex("\\[.*?\\]")

    //Debug to verify Byte conversion works
    //val value2 = value.toString.getBytes("UTF-8")
    if (value.isInstanceOf[String]) {
      sentence = value.asInstanceOf[String]
    } else {
      // Kafka returns bytes
      val bytes = value.asInstanceOf[Array[Byte]]
      try {
        sentence = new String(bytes.map(_.toChar))
      } catch {
        case e: UnsupportedEncodingException => throw new RuntimeException(e)
      }
    }

    val words = sentence.split("\\s+") take 5 drop 4
    //val words = pattern findAllIn sentence
    println(words.mkString)
    for {
      word <- words
    } yield collector.emit(new Values(word))
  }
}