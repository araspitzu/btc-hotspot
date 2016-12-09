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

package protocol

import java.sql.Timestamp
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration.DbConfig._
import commons.TestData
import commons.Helpers._
import java.time.LocalDateTime

import protocol.domain.{Offer, Session}
import protocol.domain.QtyUnit.QtyUnit
import protocol.domain.QtyUnit
import slick.driver.H2Driver.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by andrea on 17/11/16.
  */
trait DatabaseComponent extends LazyLogging {

  val database:Database
  
  class Database {
    val db = {
      logger.info(s"Opening database for conf '$configPath' @ $jdbcUrl")
      Database.forConfig(configPath)
    }
  
    db.run({
      logger.info(s"Setting up schemas and populating tables")
      DBIO.seq (
        (OfferRepository.offersTable.schema ++
          SessionRepository.sessionsTable.schema).create,
      
        //Insert some offers
        OfferRepository.offersTable ++= TestData.offers
      )
    })
  
    addShutDownHook {
      logger.info("Shutting down db")
      Await.result( db.shutdown, Duration(2, "seconds") )
    }
  }
  
  
}