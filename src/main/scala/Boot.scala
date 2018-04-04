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
import registry.{ DatabaseRegistry, MiniPortalRegistry }
import resources.admin.AdminPanelRegistry
import wallet.WalletServiceRegistry

import scala.util.{ Failure, Success, Try }

object Boot extends App with LazyLogging {

  try {
    logger.info(s"Starting btc-hotspot")
    AdminPanelRegistry.start
    MiniPortalRegistry.start
    DatabaseRegistry.start
  } catch {
    case thr: Throwable => logger.error("Initialization error", thr)
  } finally {
    logger.info(s"Done booting.")
  }

}

