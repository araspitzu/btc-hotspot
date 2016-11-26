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
import org.joda.time.{LocalDateTime}
import protocol.domain.{QtyUnit, Session, Offer}
import protocol.domain.QtyUnit._
import scala.collection.mutable
import slick.driver.H2Driver.api._

import scala.concurrent.Future

/**
  * Created by andrea on 17/11/16.
  */
package object Repository extends LazyLogging {

  private val db = Database.forConfig(s"db.$env")

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run:Unit ={
      logger.info("Shutting down db")
      db.shutdown.wait(2000)
    }
  })

  object SessionRepository {

    class SessionTable(tag: Tag) extends Table[Session](tag, "SESSIONS"){
      implicit val localDateTimeMapper = MappedColumnType.base[LocalDateTime,SQLDate](
        localDateTime => new SQLDate(localDateTime.toDateTime.toDate.getTime),
        date => new LocalDateTime(date)
      )

      def id = column[String]("id", O.PrimaryKey)
      def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATE")) //Gets mapped to java.sql.Date
      def clientMac = column[String]("clientMac")
      def remainingUnits = column[Long]("remainingUnits")

      override def * = (id, createdAt, clientMac, remainingUnits) <> (Session.tupled, Session.unapply)
    }

    val sessionsTable = TableQuery[SessionTable]

    // returns number of modified rows
    def insert(session: Session):Future[Int] = db.run {
      sessionsTable.insertOrUpdate(session)
    }

    val byMacAddress = db.run {
      sessionsTable.delete
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


    def insert(offer: Offer) = {
      offersTable.insertOrUpdate(offer)
    }

    def allOffers:Future[Int] = db.run {
      offersTable.length.result
    }


  }


  private val offerCache = new mutable.HashMap[String, Offer]()
  private val sessionOfferCache = new mutable.HashMap[Session, Option[Offer]]()
  private val sessionMacCache = new mutable.HashMap[String, Session]()

  val offer1 = Offer(
    qty = 10,
    qtyUnit = minutes,
    price = 350000,
    description =  "10 minutes"
  )
  val offer2 = Offer(
    qty = 20,
    qtyUnit = minutes,
    price = 450000,
    description =  "20 minutes"
  )
  val offer3 = Offer(
    qty = 30,
    qtyUnit = minutes,
    price = 500000,
    description =  "30 minutes"
  )

  offerCache.put(offer1.offerId, offer1)
  offerCache.put(offer2.offerId, offer2)
  offerCache.put(offer3.offerId, offer3)


  def allOffers = offerCache.values.toSeq

  def offerById(offerId:String) = offerCache(offerId)

  def allSessions = sessionMacCache.values.toSeq

  def insertSessionForMac(session:Session, mac:String) = sessionMacCache.put(mac, session)

  def sessionByMac(mac:String) = sessionMacCache.get(mac)


}