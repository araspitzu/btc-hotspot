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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration.config
import commons.TestData
import iptables.IpTablesServiceImpl
import ln.EclairClientImpl
import protocol._
import services._
import resources.admin.AdminPanelService
import resources.miniportal.MiniPortalService
import wallet.LightningServiceImpl
import watchdog.SchedulerImpl

object Boot extends App with LazyLogging {

  logger.info(s"Starting btc-hotspot")
  try {

    implicit val system = ActorSystem(config.getString("akka.actorSystem"))
    implicit val executionContext = system.dispatcher
    implicit val fm = ActorMaterializer()

    object env {
      val database = new DatabaseImpl
      import database.database.profile.api._

      val eclairClient = new EclairClientImpl

      val ipTablesService = new IpTablesServiceImpl
      val schedulerService = new SchedulerImpl

      val offerRepository = new OfferRepositoryImpl(database)
      val sessionRepository = new SessionRepositoryImpl(database, offerRepository)
      val invoiceRepository = new InvoiceRepositoryImpl(database, sessionRepository)

      //Setup DB
      database.database.db.run({
        logger.info(s"Setting up schemas and populating tables")
        DBIO.seq(
          (offerRepository.offersTable.schema ++
            sessionRepository.sessionsTable.schema ++
            invoiceRepository.invoiceTable.schema).create,

          //Insert some offers
          offerRepository.offersTable ++= TestData.offers
        )
      })

      val sessionService = new SessionServiceImpl(this)
      val invoiceService = new InvoiceServiceImpl(this)
      val walletService = new LightningServiceImpl(this)
      val adminService = new AdminServiceImpl(this)

      val miniPortal = new MiniPortalService(this)
      val adminPanel = new AdminPanelService(this)
    }

    val start = env

  } catch {
    case thr: Throwable => logger.error("Initialization error", thr)
  } finally {
    logger.info(s"Done booting.")
  }

}

