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

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration.MiniPortalConfig._
import registry.MiniPortalRegistry._
import commons.AppExecutionContextRegistry.context._
import registry.{DatabaseRegistry, MiniPortalRegistry}


object Boot extends App with LazyLogging {
 
  logger.info(s"Starting btc-hotspot")

  //Start db
  DatabaseRegistry.database
  
  //starts wallet service for miniportal
  //MiniPortalRegistry.walletService
  
  bindOrFail(miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")

  def bindOrFail(handler:Route, iface:String, port:Int, serviceName:String):Unit = {
    Http().bindAndHandle(handler, iface, port) map { binding =>
      logger.info(s"Service $serviceName bound to ${binding.localAddress}") } recover { case ex =>
      logger.info(s"Interface could not bind to $iface:$port", ex.getMessage)
      throw ex;
    }
  }



}


