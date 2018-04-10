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

package resources.miniportal

import akka.http.scaladsl.server.{ AuthorizationFailedRejection, Route, ValidationRejection }
import registry.IpTablesServiceRegistry._
import resources.{ CommonResource, ExtraDirectives }

trait SessionAPI extends CommonResource with ExtraDirectives {

  def statusRoute: Route = path("api" / "session" / LongNumber) { sessionId =>
    sessionOrReject { session =>
      if (session.id != sessionId)
        reject(AuthorizationFailedRejection)
      else {
        get {
          complete(session)
        }
      }
    }
  }

  def enableMeRoute = extractClientMAC {
    _ match {
      case Some(mac) => enableMe(mac)
      case None      => reject(ValidationRejection("Mac not found"))
    }
  }

  def enableMe(macAddress: String) = get {
    path("api" / "enableme") {
      complete(ipTablesServiceImpl.enableClient(macAddress))
    } ~ path("api" / "disableme") {
      complete(ipTablesServiceImpl.disableClient(macAddress))
    }
  }

}
