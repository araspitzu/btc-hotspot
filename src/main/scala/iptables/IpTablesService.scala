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

import java.net.InetAddress

import commons.Helpers._
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.concurrent.Future
import commons.AppExecutionContextRegistry.context._
import iptables.domain.ChainEntry

/**
  * Created by andrea on 09/11/16.
  *
  */
object IpTablesService extends LazyLogging {
  
  private def iptables(params:String) = {
    s"sudo /sbin/iptables $params"
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
             source = InetAddress.getByName( words(7) ),
             destination = InetAddress.getByName( words(8) ),
             rule = words.drop(8).fold("")(_ + " "+ _)
           )
        }.toSeq
    }
    
  }
  
  def enableClient(mac:String):Future[String] = {
    iptables(s"-I internet 1 -t mangle -m mac --mac-source $mac -j RETURN").exec
  }

  def disableClient(mac:String):Future[String] = {
    iptables(s"-D internet -t mangle -m mac --mac-source $mac -j RETURN").exec
  }

}


