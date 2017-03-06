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
import protocol.domain.{Offer, Session}
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos.PaymentACK
import protocol.SessionRepositoryImpl
import protocol.domain.QtyUnit.MB
import registry.{IpTablesServiceRegistry, SchedulerRegistry, SessionRepositoryRegistry}
import wallet.{WalletServiceInterface, WalletServiceRegistry}
import watchdog.{SchedulerImpl, StopWatch, TimebasedStopWatch}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object SessionServiceRegistry extends SessionServiceComponent {
  
  val sessionService:SessionServiceInterface = new SessionServiceImpl()
  
}

trait SessionServiceComponent {
  
  val sessionService:SessionServiceInterface
  
}

trait SessionServiceInterface {
  
  def payAndEnableSessionForOffer(session: Session, offerId: Long, payment: Protos.Payment):Future[PaymentACK]
  
  def disableSession(session: Session):FutureOption[Unit]
  
  def getOrCreate(mac:String):Future[Long]
  
  def byId(id:Long):FutureOption[Session]
  
  def byMac(mac: String): FutureOption[Session]
  
  def byMacSync(mac: String): Option[Session]
  
}


class SessionServiceImpl(dependencies:{
  val sessionRepository: SessionRepositoryImpl
  val offerService:OfferServiceInterface
  val walletService: WalletServiceInterface
}) extends SessionServiceInterface with LazyLogging {
  import dependencies.walletService._
  import dependencies.sessionRepository._
  import dependencies.offerService._
  
  def this() = this(new {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val offerService:OfferServiceInterface = OfferServiceRegistry.offerService
    val walletService: WalletServiceInterface = WalletServiceRegistry.walletService
  })
  
  
  val sessionIdToStopwatch = new scala.collection.mutable.HashMap[Long, StopWatch]
  
  def selectStopwatchForOffer(session: Session, offer: Offer):StopWatch = {
    
    val stopWatchDependencies = new {
      val ipTablesService = IpTablesServiceRegistry.ipTablesServiceImpl
      val scheduler: SchedulerImpl = SchedulerRegistry.schedulerImpl
    }
    
    offer.qtyUnit match {
      case MB => ???
      case millis => new TimebasedStopWatch(stopWatchDependencies, session, offer.qty)
    }
  }
  
  def payAndEnableSessionForOffer(session: Session, offerId: Long, payment: Protos.Payment): Future[PaymentACK] = {
    logger.info(s"Paying session ${session.id} for offer $offerId")
    for {
      paymentAck <- validateBIP70Payment(payment) orFailWith "Payment error"
      offer <- offerById(offerId) orFailWith "Offer not found"
      _ <- upsert(session.copy(offerId = Some(offerId), remainingUnits = offer.qty)).future
      stopWatch = selectStopwatchForOffer(session, offer)
      _ <- stopWatch.start(onLimitReach = { disableSession(session) }).future
    } yield {
      sessionIdToStopwatch += session.id -> stopWatch
      logger.info(s"Enabled session ${session.id} for offer $offerId")
      paymentAck
    }
  }
  
  def disableSession(session: Session):FutureOption[Unit] = {
    for {
      stopWatch <- FutureOption(Future.successful(sessionIdToStopwatch.get(session.id)))
    } yield {
      stopWatch.stop
      sessionIdToStopwatch.remove(session.id)
    }
  }

  def byId(id:Long):FutureOption[Session] = bySessionId(id)
  
  def byMac(mac: String): FutureOption[Session] = byMacAddress(mac)

  //TODO fucking remove!
  def byMacSync(mac: String): Option[Session] = {
    Await.result(byMac(mac).future, 10 seconds)
  }

  /*
    Returns the id of the existing session for this mac, create a new one if
    no session can be found, ids are created by the database
   */
  def getOrCreate(mac:String):Future[Long] = {
    byMac(mac).future flatMap {
      case Some(session) =>
        Future.successful(session.id)
      case None => insert(Session(clientMac = mac)) map { sessionId =>
          logger.info(s"Created session $sessionId for $mac")
          sessionId
        }
    }
  }


}
