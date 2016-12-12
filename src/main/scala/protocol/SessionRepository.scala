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

import commons.Helpers.FutureOption
import protocol.domain.{Offer, Session}
import registry.DatabaseRegistry._
import slick.driver.H2Driver.api._
import commons.AppExecutionContextRegistry.context._
import scala.concurrent.Future

/**
  * Created by andrea on 09/12/16.
  */
object SessionRepository {
  this:DatabaseComponent =>
  
  import database.db
  
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
  
  def upsert(session: Session):FutureOption[Session] = db.run {
    sessionsTable
      .filter(_.id === session.id)
      .update(session) map {
        case 0 => None
        case _ => Some(session)
    }
      
  }
  
  def byIdWithOffer(id:Long):FutureOption[(Session, Offer)] = db.run {
    sessionsTable
      .filter(_.id === id)
      .join(OfferRepository.offersTable).on( (s,o) => s.offerId.map(_ === o.offerId) )
      .result
      .headOption
  }
  
  def bySessionId(id:Long):FutureOption[Session] = db.run {
    sessionsTable
      .filter(_.id === id)
      .result
      .headOption
  }
  
  def allSession:Future[Seq[Session]] = db.run {
    sessionsTable
      .result
  }
  
  def byMacAddress(mac:String):FutureOption[Session] = db.run {
    sessionsTable
      .filter(_.clientMac === mac)
      .result
      .headOption
  }
  
  def activeSessions:Future[Seq[Session]] = db.run {
    sessionsTable
      .filter(_.remainingUnits > 0L)
      .result
  }
  
  
}

