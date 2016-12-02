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

import scala.io.Source
import commons.Configuration._

/**
 * Created by andrea on 16/11/16.
 */
//TODO add caching
object ArpService {

  private final val arpFile = "/proc/net/arp"

  def arpLookup(ipAddr:String):Option[String] = {
    if(env == "local") return Some("unknown")
    
    Source
      .fromFile(arpFile)
      .getLines
      .drop(1)
      .find(_.startsWith(ipAddr)).map(_.substring(41,58))
  }

}
