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

import com.typesafe.scalalogging.LazyLogging
import protocol.{ DatabaseImpl, InvoiceRepositoryImpl, OfferRepositoryImpl, SessionRepositoryImpl }
import services.SessionServiceImpl

object Boot extends App with LazyLogging {

  // try {
  logger.info(s"Starting btc-hotspot")
  val database = new DatabaseImpl

  val offerRepository = new OfferRepositoryImpl(database)
  val sessionRepository = new SessionRepositoryImpl(database, offerRepository)
  val invoiceRepository = new InvoiceRepositoryImpl(database, sessionRepository)

  val sessionService = new SessionServiceImpl(this)

  //  } catch {
  //    case thr: Throwable => logger.error("Initialization error", thr)
  //  } finally {
  //    logger.info(s"Done booting.")
  //  }

}

