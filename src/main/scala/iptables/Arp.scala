package iptables

import scala.io.Source


/**
 * Created by andrea on 16/11/16.
 */
//TODO add caching
object Arp {

  private final val arpFile = "/proc/net/arp"

  def arpLookup(ipAddr:String):Option[String] = {
    Source
      .fromFile(arpFile)
      .getLines
      .find(_.startsWith(ipAddr)).map(_.substring(41,58))
  }

}
