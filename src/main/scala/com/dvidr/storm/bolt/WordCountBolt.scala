package com.dvidr.storm.bolt

import com.twitter.algebird._
import backtype.storm.task.OutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.Fields
import backtype.storm.tuple.Tuple
import backtype.storm.tuple.Values
import java.util.{Map => JMap}

class WordCountBolt extends BaseRichBolt {
  val mm = new MapMonoid[String, Int]()
  var collector: OutputCollector = _
  var countMap = Map[String, Int]()

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    this.collector = collector
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("word", "count"))
  }

  override def execute(tuple: Tuple) {
    val word = tuple.getString(0)
    countMap = mm.plus(countMap, Map((word, 1)))
    collector.emit(new Values(word, Int.box(countMap(word))))
  }
}

