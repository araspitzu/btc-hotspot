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

package services

import com.typesafe.scalalogging.LazyLogging
import protocol.domain.{ Offer, Session }
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import protocol.{ InvoiceRepositoryImpl, OfferRepositoryComponent, OfferRepositoryImpl, SessionRepositoryImpl }
import protocol.domain.QtyUnit._
import registry._
import wallet.{ WalletServiceInterface, WalletServiceRegistry }
import watchdog.{ SchedulerImpl, StopWatch, TimebasedStopWatch }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object SessionServiceRegistry extends SessionServiceComponent {

  val sessionService: SessionServiceInterface = new SessionServiceImpl

}

trait SessionServiceComponent {

  val sessionService: SessionServiceInterface

}

trait SessionServiceInterface {

  def enableSessionForInvoice(session: Session, invoiceId: Long): Future[Long]

  def disableSession(session: Session): FutureOption[Unit]

  def getOrCreate(mac: String): Future[Long]

  def byId(id: Long): FutureOption[Session]

  def byMac(mac: String): FutureOption[Session]

  def byMacSync(mac: String): Option[Session]

  def activeSessionIds(): Seq[Long]

}

class SessionServiceImpl(dependencies: {
  val sessionRepository: SessionRepositoryImpl
  val invoiceRepository: InvoiceRepositoryImpl
  val offerRepository: OfferRepositoryImpl
  val walletService: WalletServiceInterface
}) extends SessionServiceInterface with LazyLogging {

  private def offerRepository = dependencies.offerRepository
  private def sessionRepository = dependencies.sessionRepository
  private def invoiceRepository = dependencies.invoiceRepository
  //FIXME unused
  private def walletService = dependencies.walletService

  def this() = this(new {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val invoiceRepository: InvoiceRepositoryImpl = InvoiceRepositoryRegistry.invoiceRepositoryImpl
    val offerRepository: OfferRepositoryImpl = OfferRepositoryRegistry.offerRepositoryImpl
    val walletService: WalletServiceInterface = WalletServiceRegistry.walletService
  })

  val sessionIdToStopwatch = new scala.collection.mutable.HashMap[Long, StopWatch]

  def selectStopwatchForOffer(session: Session, offer: Offer): StopWatch = {

    val stopWatchDependencies = new {
      val ipTablesService = IpTablesServiceRegistry.ipTablesServiceImpl
      val scheduler: SchedulerImpl = SchedulerRegistry.schedulerImpl
    }

    offer.qtyUnit match {
      case MB     => ???
      case millis => new TimebasedStopWatch(stopWatchDependencies, session, offer.qty)
    }
  }

  def enableSessionForInvoice(session: Session, invoiceId: Long): Future[Long] = {
    logger.info(s"Enabling session ${session.id} for invoice $invoiceId")
    for {
      invoice <- invoiceRepository.invoiceById(invoiceId) orFailWith s"Invoice $invoiceId not found"
      offer <- offerRepository.byId(invoice.offerId.get) orFailWith s"Offer ${invoice.offerId.get} not found"
      _ = if (!invoice.paid) throw new IllegalStateException(s"Unable to enable session ${session.id}, invoice $invoiceId NOT PAID!")
      _ <- sessionRepository.upsert(session.copy(offerId = Some(offer.offerId), remainingUnits = offer.qty)).future
      stopWatch = selectStopwatchForOffer(session, offer)
      _ <- stopWatch.start(onLimitReach = { disableSession(session) }).future
    } yield {
      sessionIdToStopwatch += session.id -> stopWatch
      logger.info(s"Enabled session ${session.id} for invoice $invoiceId ")
      session.id
    }
  }

  def disableSession(session: Session): FutureOption[Unit] = {
    logger.info(s"Disabling session ${session}")
    for {
      _ <- sessionRepository.upsert(session.copy(offerId = None))
      stopWatch <- FutureOption(Future.successful(sessionIdToStopwatch.get(session.id)))
    } yield {
      stopWatch.stop
      sessionIdToStopwatch.remove(session.id)
    }
  }

  def activeSessionIds(): Seq[Long] = sessionIdToStopwatch.keys.toSeq

  def byId(id: Long): FutureOption[Session] = sessionRepository.bySessionId(id)

  def byMac(mac: String): FutureOption[Session] = sessionRepository.byMacAddress(mac)

  //TODO fucking remove!
  def byMacSync(mac: String): Option[Session] = {
    Await.result(byMac(mac).future, 10 seconds)
  }

  /*
    Returns the id of the existing session for this mac, create a new one if
    no session can be found, ids are created by the database
   */
  def getOrCreate(mac: String): Future[Long] = {
    byMac(mac).future flatMap {
      case Some(session) =>
        Future.successful(session.id)
      case None => sessionRepository.insert(Session(clientMac = mac)) map { sessionId =>
        logger.info(s"Created session $sessionId for $mac")
        sessionId
      }
    }
  }

}
