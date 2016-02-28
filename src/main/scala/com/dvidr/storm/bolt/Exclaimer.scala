package com.dvidr.storm.bolt

/**
  * Created by galois on 2/23/16.
  */
object Exclaimer {
  def exclaim(s: String): String = {
    s + "!!!"
  }
}