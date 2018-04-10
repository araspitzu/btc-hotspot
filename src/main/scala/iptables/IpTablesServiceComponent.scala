/*
 * btc-hotspot
 * Copyright (C) 2016  Andrea Raspitzu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package iptables

import com.typesafe.scalalogging.LazyLogging
import commons.Helpers.FutureOption
import commons.Helpers.CmdExecutor
import scala.concurrent.Future
import commons.AppExecutionContextRegistry.context._
import iptables.domain.ChainEntry
import commons.Configuration._

trait IpTablesServiceComponent {

  val ipTablesServiceImpl: IpTablesInterface

}

trait IpTablesInterface {

  def enableClient(mac: String): Future[String]

  def disableClient(mac: String): Future[String]

}

class IpTablesServiceImpl extends IpTablesInterface with LazyLogging {

  private def iptables(params: String) = {
    s"sudo /sbin/iptables $params"
  }

  def report: Future[Seq[ChainEntry]] = {
    iptables("-t mangle -nvxL internet").exec.map {
      _.lines
        .drop(2) //drop header and column header
        .map { r => //iterate over each rule
          val words = r.split(" ").filter(_ != "") //extract words
          ChainEntry(
            pkts = words(0).toLong,
            bytes = words(1).toLong,
            target = words(2),
            prot = words(3),
            opt = words(4),
            in = words(5),
            out = words(6),
            source = words(7),
            destination = words(8),
            rule = words.drop(8).fold("")(_+" "+_)
          )
        }.toSeq
    }

  }

  override def enableClient(mac: String) = env match {
    case "hotspot" => iptables(s"-t mangle -I internet_outgoing 1 -m mac --mac-source $mac -j RETURN").exec
    case "local"   => Future.successful("")
  }

  override def disableClient(mac: String) = env match {
    case "hotspot" => iptables(s"-t mangle -D internet_outgoing -m mac --mac-source $mac -j RETURN").exec
    case "local"   => Future.successful("")
  }

  /*
    Set 10MB quota per IP
    $IPTABLES -t mangle -A internet_in -d 192.168.0.67 -m quota ! --quota 10000000 -j DROP
   */
}

