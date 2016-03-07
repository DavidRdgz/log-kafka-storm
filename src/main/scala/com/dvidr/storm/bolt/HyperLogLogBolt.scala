package com.dvidr.storm.bolt

import java.util.{Map => JMap}

import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.{Values, Fields, Tuple}
import com.twitter.algebird._

class HyperLogLogBolt extends BaseRichBolt {
  var hll: HyperLogLogMonoid = _
  var h: HLL = _
  var collector: OutputCollector = _

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    val hll = new HyperLogLogMonoid(16)
    this.hll = hll
    this.h = hll.zero
    this.collector = collector
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("word", "count"))
  }

  override def execute(tuple: Tuple) {
    val word = tuple.getString(0)
    val wbytes = word.toCharArray.map(_.toByte)
    val hlls = hll(wbytes)
    h += hlls
    println(hll.sizeOf(h).estimate.toInt)
    collector.emit(new Values(word, Int.box(hll.sizeOf(h).estimate.toInt)))

  }
}

