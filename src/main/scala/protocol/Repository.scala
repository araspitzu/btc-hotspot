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
object Repository extends LazyLogging {

  private val db = {
    logger.info(s"Opening database for conf '$configPath' @ localhost:$dbmsPort")
    org.h2.tools.Server.createTcpServer("-tcpAllowOthers", "-tcpPort", dbmsPort).start() //starts h2 in server mode
    
    if(webUI) {
      logger.info(s"Creating web ui @ localhost:8888")
      org.h2.tools.Server.createWebServer( "-webPort", "8888").start()
    }
    
    Database.forConfig(configPath)
  }
  
  val dbSetup = DBIO.seq (
    (OfferRepository.offersTable.schema ++
      SessionRepository.sessionsTable.schema).create,
    
    //Insert some offers
    OfferRepository.offersTable ++= TestData.offers
  )
  
  lazy val setupDb = db.run({
    logger.info(s"Setting up schemas and populating tables")
    dbSetup
  })

  addShutDownHook {
    logger.info("Shutting down db")
    Await.result( db.shutdown, Duration(2, "seconds") )
  }
  

  object SessionRepository {


    class SessionTable(tag: Tag) extends Table[Session](tag, "SESSIONS"){
      
      implicit val localDateTimeMapper = MappedColumnType.base[LocalDateTime,Timestamp](
        localDateTime => Timestamp.valueOf(localDateTime),
        date => date.toLocalDateTime
      )

      def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATETIME")) //mapped via java.time.LocalDateTime -> java.sql.Timestamp -> DATATYPE(DATETIME)
      def clientMac = column[String]("clientMac")
      def remainingUnits = column[Long]("remainingUnits")
      def offerId = column[Option[Long]]("offerId")

      def offer = foreignKey("offerFK", offerId, OfferRepository.offersTable)(_.offerId.?)
      
      override def * = (id, createdAt, clientMac, remainingUnits, offerId) <> (Session.tupled, Session.unapply)
    }

    val sessionsTable = TableQuery[SessionTable]

    def insert(session: Session):Future[Long] = db.run {
      (sessionsTable returning sessionsTable.map(_.id)) += session
    }
    
    def updateSessionWithOffer(session: Session, offer: Option[Offer]):Future[Int] = db.run {
      sessionsTable
        .update(session.copy(offerId = offer.map(_.offerId)))
    }
    
    def byId(id:Long):Future[Option[Session]] = db.run {
      sessionsTable
        .filter(_.id === id)
        .result
        .headOption
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
    
    def activeSessions:Future[Seq[Session]] = db.run {
      sessionsTable
        .filter(_.remainingUnits > 0L)
        .map(identity)
        .result
    }


  }

  object OfferRepository {

    class OfferTable(tag:Tag) extends Table[Offer](tag,"OFFERS"){

      implicit val qtyUnitMapper = MappedColumnType.base[QtyUnit, String](
        e => e.toString,
        s => QtyUnit.withName(s)
      )

      def offerId = column[Long]("offerId", O.PrimaryKey, O.AutoInc)
      def qty = column[Long]("qty")
      def qtyUnit = column[QtyUnit]("qtyUnit")
      def price = column[Long]("price")
      def description = column[String]("description")

      override def * = (offerId, qty, qtyUnit, price, description) <> (Offer.tupled, Offer.unapply)
    }

    val offersTable = TableQuery[OfferTable]

    def byId(id:Long):Future[Option[Offer]] = db.run {
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