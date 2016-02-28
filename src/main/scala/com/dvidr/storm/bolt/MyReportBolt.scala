package com.dvidr.storm.bolt

import backtype.storm.task.OutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.{Tuple}
import java.util.{Map => JMap}

class MyReportBolt extends BaseRichBolt {
  var collector: OutputCollector = _
  var counts = Map[String, Int]()

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    this.collector = collector
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    // Nothing to declare this Bolt is a sink.
  }

  override def execute(tuple: Tuple) {
    val word = tuple.getString(0)
    val count = tuple.getInteger(1)
    counts = counts + (word -> count)
    //println("---- CURRENT COUNTS ----")
    //println(counts)
  }

  override def cleanup() {
    println("--- FINAL COUNTS ---")
    for {
      key <- counts.keys
    } yield println(key + " : " + counts.get(key))
    println("--------------")
  }
}

