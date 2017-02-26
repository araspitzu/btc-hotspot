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
import java.time.LocalDateTime

import com.typesafe.scalalogging.LazyLogging
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import protocol.domain.{Offer, Session => DomainSession}
import registry.{DatabaseRegistry, OfferRepositoryRegistry}
import scala.concurrent.Future

trait SessionRepositoryComponent {
  
  val sessionRepositoryImpl: SessionRepositoryImpl
  
}

class SessionRepositoryImpl extends LazyLogging {
  import DatabaseRegistry.database.database.profile.api._
  
  lazy val db:Database = DatabaseRegistry.database.db

  class SessionTable(tag: Tag) extends Table[DomainSession](tag, "SESSIONS"){
  
    implicit val localDateTimeMapper = MappedColumnType.base[LocalDateTime,Timestamp](
      localDateTime => Timestamp.valueOf(localDateTime),
      date => date.toLocalDateTime
    )
  
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATETIME")) //mapped via java.time.LocalDateTime -> java.sql.Timestamp -> DATATYPE(DATETIME)
    def clientMac = column[String]("clientMac")
    def remainingUnits = column[Long]("remainingUnits")
    def offerId = column[Option[Long]]("offerId")
  
    def offer = foreignKey("offerFK", offerId, OfferRepositoryRegistry.offerRepositoryImpl.offersTable)(_.offerId.?)
  
    override def * = (id, createdAt, clientMac, remainingUnits, offerId) <> (DomainSession.tupled, DomainSession.unapply)
  }

  val sessionsTable = TableQuery[SessionTable]

  def insert(DomainSession: DomainSession):Future[Long] = db.run {
    (sessionsTable returning sessionsTable.map(_.id)) += DomainSession
  }

  def upsert(session: DomainSession):FutureOption[Long] = {
    logger.warn("DOING UPSERT")
    db.run {
      (sessionsTable returning sessionsTable.map(_.id)).insertOrUpdate(session)
    }
  }

  def byIdWithOffer(id:Long):FutureOption[(DomainSession, Offer)] = db.run {
    sessionsTable
      .filter(_.id === id)
      .join(OfferRepositoryRegistry.offerRepositoryImpl.offersTable).on( (s,o) => s.offerId.map(_ === o.offerId) )
      .result
      .headOption
  }

  def bySessionId(id:Long):FutureOption[DomainSession] = db.run {
    sessionsTable
      .filter(_.id === id)
      .result
      .headOption
  }

  def allSession:Future[Seq[DomainSession]] = db.run {
    sessionsTable
      .result
  }

  def byMacAddress(mac:String):FutureOption[DomainSession] = db.run {
    sessionsTable
      .filter(_.clientMac === mac)
      .result
      .headOption
  }

  def activeSessions:Future[Seq[DomainSession]] = db.run {
    sessionsTable
      .filter(_.remainingUnits > 0L)
      .result
  }


}

