package com.dvidr.storm.bolt

import java.util.{Map => JMap}

import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.{Values, Fields, Tuple}
import com.twitter.algebird.CMSHasherImplicits._
import com.twitter.algebird._

class CountMinSketchBolt extends BaseRichBolt {
  val DELTA = 1E-10
  val EPS = 0.001
  val SEED = 1
  val HVYHIT = .003
  var cms: TopPctCMSMonoid[String] = _
  var c: TopCMS[String] = _
  var collector: OutputCollector = _

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    val CMS_MONOID = TopPctCMS.monoid[String](EPS, DELTA, SEED, HVYHIT)
    this.cms = CMS_MONOID
    this.c = CMS_MONOID.zero
    this.collector = collector
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("word", "count"))
  }

  override def execute(tuple: Tuple) {
    val word = tuple.getString(0)
    val cs = cms.create(word)
    c = cms.plus(c, cs)
    collector.emit(new Values(word, Int.box(c.frequency(word).estimate.toInt)))
  }
}

