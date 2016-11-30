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

import commons.Helpers._
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.concurrent.Future
import commons.AppExecutionContextRegistry.context._

/**
  * Created by andrea on 09/11/16.
  *
  */
object IpTablesService extends LazyLogging {
  
  private def iptables(params:String) = {
    s"sudo /sbin/iptables $params"
  }

  def enableClient(mac:String):Future[String] = {
    iptables(enableClientRule(mac)).exec
  }

  def disableClient(mac:String):Future[String] = {
    iptables(disableClientRule(mac)) exec
  }

  private def enableClientRule(mac:String):String =
    s"-I internet 1 -t mangle -m mac --mac-source $mac -j RETURN"

  private def disableClientRule(mac:String):String =
    s"-D internet -t mangle -m mac --mac-source $mac -j RETURN"

}


