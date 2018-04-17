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

import akka.http.scaladsl.server.Route
import commons.Configuration.MiniPortalConfig._
import commons.AppExecutionContextRegistry.context._
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging
import iptables.{ IpTablesInterface, IpTablesServiceComponent, IpTablesServiceImpl }
import resources.miniportal.MiniPortal
import services.{ InvoiceServiceImpl, InvoiceServiceInterface, SessionServiceInterface }
import wallet.WalletServiceInterface
import watchdog.{ SchedulerComponent, SchedulerImpl }

package object registry extends LazyLogging {

  trait Registry {
    //Dummy call to trigger object initialization thus the registry instantiation
    val start = ()
  }

  trait HttpApi extends LazyLogging {
    def bindOrFail(handler: Route, iface: String, port: Int, serviceName: String): Unit = {
      Http().bindAndHandle(handler, iface, port) map { binding =>
        logger.info(s"Service $serviceName bound to ${binding.localAddress}")
      } recover {
        case ex =>
          logger.error(s"Interface could not bind to $iface:$port", ex)
          throw ex
      }
    }
  }

  class MiniPortalService(dependencies: {
    val sessionService: SessionServiceInterface
    val invoiceService: InvoiceServiceInterface
    val walletService: WalletServiceInterface
  }) extends MiniPortal with HttpApi {

    val sessionService: SessionServiceInterface = dependencies.sessionService
    val invoiceService: InvoiceServiceInterface = dependencies.invoiceService
    val walletService: WalletServiceInterface = dependencies.walletService

    bindOrFail(miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")

  }

  object SchedulerRegistry extends Registry with SchedulerComponent {
    override val schedulerImpl = new SchedulerImpl
  }

}
