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

import akka.http.scaladsl.server.Route
import commons.Configuration.AdminPanelConfig._
import commons.Configuration.NetworkConfig
import commons.{ Configuration, MailService }
import commons.MailService.Mail
import registry.{ HttpApi }
import resources.CaptiveResource
import services.{ AdminService, SessionServiceImpl }

import scala.collection.JavaConverters._

class AdminPanelService(dependencies: {
  val sessionService: SessionServiceImpl
  val adminService: AdminService
}) extends AdminPanel with HttpApi {

  val sessionService: SessionServiceImpl = dependencies.sessionService
  val adminService: AdminService = dependencies.adminService

  //Notify the user of the boot via email
  val hotspotAddress = Configuration.env match {
    case "local"   => "127.0.0.1"
    case "hotspot" => getUplinkInternalIp()
  }

  logger.info(s"Using uplink interface \'${NetworkConfig.uplinkInterfaceName}\' @ $hotspotAddress")
  bindOrFail(adminPanelRoute, hotspotAddress, adminPanelPort, "Admin Panel")

  private def getUplinkInternalIp(): String = {

    val ifaceName = NetworkConfig.uplinkInterfaceName

    val iface = java.net.NetworkInterface
      .getNetworkInterfaces.asScala.find(_.getName == ifaceName) match {
        case None                   => throw new NoSuchElementException(s"Iface $ifaceName not found")
        case Some(networkInterface) => networkInterface
      }

    val ipv4Address = iface.getInterfaceAddresses.asScala.find(inetAddress =>
      inetAddress.getNetworkPrefixLength == 24
    ) match {
      case None          => throw new NoSuchElementException(s"Unable to find ipv4 lnUri for iface $iface")
      case Some(address) => address
    }

    ipv4Address.getAddress.getHostAddress
  }

}

trait AdminPanel
  extends CaptiveResource
  with AdminAPI {

  def staticFilesRoute: Route = getFromDirectory(adminPanelStaticFilesDir)

  val adminPanelRoute: Route =
    adminRoute ~
      staticFilesRoute ~
      emptyUrlRedirect(adminPanelIndex)

}
