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

import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Helpers._

import scala.concurrent.Future
import commons.AppExecutionContextRegistry.context._
import iptables.domain.ChainEntry


trait IpTablesServiceComponent {
  
  val ipTablesServiceImpl:IpTablesInterface
  
}

trait IpTablesInterface {
  
  def enableClient(mac:String): FutureOption[String]
  
  def disableClient(mac:String): FutureOption[String]
  
}

class IpTablesServiceImpl extends IpTablesInterface with LazyLogging {
  
  private def iptables(params:String) = {
    s"sudo iptables $params"
  }
  
  def report:Future[Seq[ChainEntry]] = {
    iptables("-t mangle -nvxL internet").exec.map {
      _.lines
        .drop(2)     //drop header and column header
        .map { r =>  //iterate over each rule
        val words = r.split(" ").filter(_ != "")  //extract words
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
          rule = words.drop(8).fold("")(_ + " "+ _)
        )
      }.toSeq
    }
    
  }
  
  override def enableClient(mac:String) = {
    logger.info("CALLING IPTABLES")
    iptables(s"-t mangle -I internet 1 -m mac --mac-source $mac -j RETURN").exec.map(Some(_))
  }
  
  override def disableClient(mac:String) = {
    iptables(s"-t mangle -D internet -m mac --mac-source $mac -j RETURN").exec.map(Some(_))
  }
  
}


