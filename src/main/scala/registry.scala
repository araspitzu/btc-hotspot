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
import commons.Configuration.MiniPortalConfig.{miniPortalHost, miniPortalPort}
import commons.TestData
import protocol._
import commons.AppExecutionContextRegistry.context._
import akka.http.scaladsl.Http
import iptables.{IpTablesInterface, IpTablesServiceComponent, IpTablesServiceImpl}
import resources.miniportal.{MiniPortal, PaymentChannelAPI}
import slick.driver.JdbcProfile
import slick.jdbc
import slick.jdbc.meta.MTable
import watchdog.{SchedulerComponent, SchedulerImpl}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


package object registry {
  
  trait Registry {
    //Dummy call to trigger object initialization thus the registry instantiation
    val start = ()
  }

  
  object MiniPortalRegistry extends Registry with MiniPortal {
    
    bindOrFail(miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")
  
    def bindOrFail(handler:Route, iface:String, port:Int, serviceName:String):Unit = {
      Http().bindAndHandle(handler, iface, port) map { binding =>
        logger.info(s"Service $serviceName bound to ${binding.localAddress}")
      } recover { case ex =>
        logger.error(s"Interface could not bind to $iface:$port", ex)
        throw ex
      }
    }
    
  }
  
  object DatabaseRegistry extends Registry with DatabaseComponent {
    override val database = new DatabaseImpl
    
    import database.database.profile.api._
    
    Await.result(setupDb, 10 seconds)
    
//    def offerTable = OfferRepositoryRegistry.offerRepositoryImpl.offersTable
//    def sessionTable = SessionRepositoryRegistry.sessionRepositoryImpl.sessionsTable
    
        
    def setupDb = database.db.run({
      logger.info(s"Setting up schemas and populating tables")
      DBIO.seq (
        (OfferRepositoryRegistry.offerRepositoryImpl.offersTable.schema ++
         SessionRepositoryRegistry.sessionRepositoryImpl.sessionsTable.schema).create,
      
        //Insert some offers
        OfferRepositoryRegistry.offerRepositoryImpl.offersTable ++= TestData.offers
      )
    })
    
  }
  
  object SessionRepositoryRegistry extends Registry with SessionRepositoryComponent {
    override val sessionRepositoryImpl = new SessionRepositoryImpl
  }
  
  object OfferRepositoryRegistry extends Registry with OfferRepositoryComponent {
    override val offerRepositoryImpl = new OfferRepositoryImpl
  }
  
  object SchedulerRegistry extends Registry with SchedulerComponent {
    override val schedulerImpl = new SchedulerImpl
  }
  
  object IpTablesServiceRegistry extends Registry with IpTablesServiceComponent {
    override val ipTablesServiceImpl: IpTablesInterface = new IpTablesServiceImpl
  }
  
}
