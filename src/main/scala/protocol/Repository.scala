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

import java.sql.{Date => SQLDate}
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration._
import commons.TestData
import commons.Helpers._
import org.joda.time.LocalDateTime
import protocol.domain.{Offer, Session}
import protocol.domain.QtyUnit.QtyUnit
import protocol.domain.QtyUnit
import slick.driver.H2Driver.api._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by andrea on 17/11/16.
  */
object Repository extends LazyLogging {

  private val db = {
    val port = config.getString(s"db.$env.port")
    logger.info(s"Opening database for conf 'db.$env' @ localhost:$port")
    org.h2.tools.Server.createTcpServer("-tcpPort", port).start() //starts h2 in server mode
    Database.forConfig(s"db.$env")
  }
  
  val dbSetup = DBIO.seq (
    (OfferRepository.offersTable.schema ++
      SessionRepository.sessionsTable.schema).create,
    
    //Insert some offers
    OfferRepository.offersTable ++= TestData.offers
  )
  
  lazy val setupDb = db.run(
    {logger.info(s"Setting up schemas and populating tables"); dbSetup}
  )

  addShutDownHook {
    logger.info("Shutting down db")
    Await.result( db.shutdown, Duration(2, "seconds") )
  }
  

  object SessionRepository {

    class SessionTable(tag: Tag) extends Table[Session](tag, "SESSIONS"){
      implicit val localDateTimeMapper = MappedColumnType.base[LocalDateTime,SQLDate](
        localDateTime => new SQLDate(localDateTime.toDateTime.toDate.getTime),
        date => new LocalDateTime(date)
      )

      def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATE")) //Gets mapped to LocalDateTime -> java.sql.Date -> DATATYPE(DATE)
      def clientMac = column[String]("clientMac")
      def remainingUnits = column[Long]("remainingUnits")

      override def * = (id, createdAt, clientMac, remainingUnits) <> (Session.tupled, Session.unapply)
    }

    val sessionsTable = TableQuery[SessionTable]

    def insert(session: Session):Future[Long] = db.run {
      (sessionsTable returning sessionsTable.map(_.id)) += session
    }

    def allSession:Future[Seq[Session]] = db.run {
      sessionsTable
        .map(identity)
        .result
    }

    def byMacAddress(mac:String):Future[Option[Session]] = db.run {
      sessionsTable
        .filter(_.clientMac === mac)
        .map(identity)
        .result
        .headOption
    }


  }

  object OfferRepository {

    class OfferTable(tag:Tag) extends Table[Offer](tag,"OFFERS"){

      implicit val qtyUnitMapper = MappedColumnType.base[QtyUnit, String](
        e => e.toString,
        s => QtyUnit.withName(s)
      )

      def offerId = column[String]("offerId", O.PrimaryKey)
      def qty = column[Long]("qty")
      def qtyUnit = column[QtyUnit]("qtyUnit")
      def price = column[Long]("price")
      def description = column[String]("description")

      override def * = (offerId, qty, qtyUnit, price, description) <> (Offer.tupled, Offer.unapply)
    }

    val offersTable = TableQuery[OfferTable]

    def byId(id:String):Future[Option[Offer]] = db.run {
      offersTable
        .filter(_.offerId === id)
        .map(identity)
        .result
        .headOption
    }

    def insert(offer: Offer):Future[Int] = db.run {
      offersTable
        .insertOrUpdate(offer)
    }

    def allOffers:Future[Seq[Offer]] = db.run {
      offersTable.map(identity).result
    }

  }

}