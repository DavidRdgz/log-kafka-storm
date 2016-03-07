package com.dvidr.storm.topology

import backtype.storm.topology.TopologyBuilder
import backtype.storm.tuple.Fields
import backtype.storm.utils.Utils
import backtype.storm.{Config, LocalCluster, StormSubmitter}
import com.dvidr.storm.bolt.{HyperLogLogBolt, MyReportBolt, SplitSentenceBolt}
import storm.kafka.{KafkaSpout, SpoutConfig, ZkHosts}

object UniqueTopicCountTopology {
  val SENTENCE_SPOUT_ID = "kafka-sentence-spout"
  val SPLIT_BOLT_ID = "split-bolt"
  val COUNT_BOLT_ID = "hyperloglog-count-bolt"
  val REPORT_BOLT_ID = "report-bolt"
  val TOPOLOGY_NAME = "unique-topic-count-topology"

  def main(args: Array[String]) {
    val builder: TopologyBuilder = new TopologyBuilder()

    builder.setSpout(SENTENCE_SPOUT_ID, buildKafkaSentenceSpout(), 1)
    builder.setBolt(SPLIT_BOLT_ID, new SplitSentenceBolt()).shuffleGrouping(SENTENCE_SPOUT_ID)
    builder.setBolt(COUNT_BOLT_ID, new HyperLogLogBolt()).fieldsGrouping(SPLIT_BOLT_ID, new Fields("word"))
    builder.setBolt(REPORT_BOLT_ID, new MyReportBolt()).globalGrouping(COUNT_BOLT_ID)

    val config = new Config()
    //config.setDebug(true)

    if (args != null && args.length > 0) {
      config.setNumWorkers(3)
      StormSubmitter.submitTopology(args(0), config, builder.createTopology())
    } else {

      val cluster: LocalCluster = new LocalCluster()
      cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology())
      Utils.sleep(10000)
      cluster.killTopology(TOPOLOGY_NAME)
      cluster.shutdown()
    }
  }

  def buildKafkaSentenceSpout(): KafkaSpout = {
    val zkHostPort = "localhost:2181"
    val topic = "sys_logs"
    val zkRoot = "/kafka-sentence-spout"
    val zkSpoutId = "sentence-spout"

    val zkHosts = new ZkHosts(zkHostPort)
    val spoutCfg = new SpoutConfig(zkHosts, topic, zkRoot, zkSpoutId)
    val kafkaSpout = new KafkaSpout(spoutCfg)

    kafkaSpout
  }
}