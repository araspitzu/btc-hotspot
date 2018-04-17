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

import java.time.LocalDateTime
import com.typesafe.scalalogging.LazyLogging
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import protocol.domain.{ Offer, Session => DomainSession }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait SessionRepositoryComponent {

  val sessionRepositoryImpl: SessionRepositoryImpl

}

class SessionRepositoryImpl(val databaseComponent: DatabaseImpl, val offerRepository: OfferRepositoryImpl) extends DbSerializers with LazyLogging {

  import databaseComponent.database.profile.api._
  private lazy val db = databaseComponent.database.db

  protected class SessionTable(tag: Tag) extends Table[DomainSession](tag, "SESSIONS") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATETIME")) //mapped via java.time.LocalDateTime -> java.sql.Timestamp -> DATATYPE(DATETIME)
    def clientMac = column[String]("clientMac")
    def remainingUnits = column[Long]("remainingUnits")
    def offerId = column[Option[Long]]("offerId")

    def offer = foreignKey("offerFK", offerId, offerRepository.offersTable)(_.offerId.?)

    override def * = (id, createdAt, clientMac, remainingUnits, offerId) <> (DomainSession.tupled, DomainSession.unapply)
  }

  val sessionsTable = TableQuery[SessionTable]

  def insert(DomainSession: DomainSession): Future[Long] = db.run {
    (sessionsTable returning sessionsTable.map(_.id)) += DomainSession
  }

  def upsert(session: DomainSession): FutureOption[Long] = {
    logger.info(s"UPSERTING...")
    val action = db.run {
      (sessionsTable returning sessionsTable.map(_.id)).insertOrUpdate(session)
    }

    action.onComplete {
      case Failure(thr: Throwable) =>
        logger.error(s"ERROR UPSERTING SESSION:", thr)
      case Success(other) =>
        logger.info(s"Upsert completed with $other")
        other
    }

    action
  }
  def byIdWithOffer(id: Long): FutureOption[(DomainSession, Offer)] = db.run {
    sessionsTable
      .filter(_.id === id)
      .join(offerRepository.offersTable).on((s, o) => s.offerId.map(_ === o.offerId))
      .result
      .headOption
  }

  def bySessionId(id: Long): FutureOption[DomainSession] = db.run {
    sessionsTable
      .filter(_.id === id)
      .result
      .headOption
  }

  def byIdSet(queryIdSet: Set[Long]): Future[Seq[DomainSession]] = db.run {
    sessionsTable
      .filter(_.id inSet queryIdSet)
      .result
  }

  def allSession: Future[Seq[DomainSession]] = db.run {
    sessionsTable
      .result
  }

  def byMacAddress(mac: String): FutureOption[DomainSession] = db.run {
    sessionsTable
      .filter(_.clientMac === mac)
      .result
      .headOption
  }

  def activeSessions: Future[Seq[DomainSession]] = db.run {
    sessionsTable
      .filter(_.remainingUnits > 0L)
      .result
  }

}

