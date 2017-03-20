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

package resources.admin

import akka.http.scaladsl.server.{MalformedQueryParamRejection, Route}
import commons.JsonSupport
import resources.CommonResource
import services.AdminServiceRegistry

trait AdminAPI extends CommonResource with JsonSupport {
  
  def adminRoute:Route =
    walletRoute ~
    sessionRoute
  
  def sessionRoute:Route = get {
    path("api" / "admin" / "session"){
      pathEnd {
        parameter("filter") { filter:String =>
          complete { filter match {
             case "all" => AdminServiceRegistry.adminService.allSessions
             case "active" => AdminServiceRegistry.adminService.activeSessions
             case unknown => reject(MalformedQueryParamRejection("filter", s"$unknown not a valid filter"))
           }
         }
        }
      }
    }
  }
  
  def walletRoute :Route = {
    pathPrefix("api" / "admin" / "wallet") {
      path("balance") {
         get {
          complete(s"${AdminServiceRegistry.adminService.walletBalance}")
        }
      } ~ path("transactions"){
         get {
           complete(AdminServiceRegistry.adminService.transactions)
         }
      }
    }
  }
  
}
