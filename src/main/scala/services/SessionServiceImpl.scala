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
import protocol.SessionRepositoryImpl
import protocol.domain.QtyUnit.MB
import registry.{IpTablesServiceRegistry, SchedulerRegistry, SessionRepositoryRegistry}
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
  
  def enableSessionFor(session: Session, offerId:Long):FutureOption[Unit]
  
  def disableSession(session: Session):FutureOption[Unit]
  
  def getOrCreate(mac:String):Future[Long]
  
  def byId(id:Long):FutureOption[Session]
  
  def byMac(mac: String): FutureOption[Session]
  
  def byMacSync(mac: String): Option[Session]
  
}


class SessionServiceImpl(dependencies:{
  val sessionRepository: SessionRepositoryImpl
  val offerService:OfferServiceInterface
}) extends SessionServiceInterface with LazyLogging {
  import dependencies._
  
  def this() = this(new {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val offerService:OfferServiceInterface = OfferServiceRegistry.offerService
  })
  
  
  //<statefulness>
  val sessionIdToStopwatch = new scala.collection.mutable.HashMap[Long, StopWatch]
  //</statefulness>
  
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
  
  
  def enableSessionFor(session: Session, offerId:Long):FutureOption[Unit] = {
    logger.warn(s"ENABLING SESSION ${session.id} FOR OFFER $offerId")
    for {
      offer <- offerService.offerById(offerId)
      _ = logger.info("OFFER RETRIEVED")
      upsertedId <- sessionRepository.upsert(session.copy(
        offerId = Some(offerId),
        remainingUnits = if(session.remainingUnits < 0) offer.qty else session.remainingUnits
      ))
      _ = logger.warn("SESSION UPDATED, SELECTING THE STOPWATCH")
      stopWatch = selectStopwatchForOffer(session, offer)
      _ = logger.warn("GOING TO START THE STOPWATCH NOW")
      res <- stopWatch.start(onLimitReach = {
        logger.info(s"Reached limit for session $upsertedId, disabling it")
        disableSession(session)
      })
    } yield {
      sessionIdToStopwatch += upsertedId -> stopWatch
      logger.info(s"Enabled session ${upsertedId} for offer $offerId")
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

  def byId(id:Long):FutureOption[Session] = sessionRepository.bySessionId(id)
  
  def byMac(mac: String): FutureOption[Session] = sessionRepository.byMacAddress(mac)

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
      case None => sessionRepository.insert(Session(clientMac = mac)) map { sessionId =>
          logger.info(s"Created session $sessionId for $mac")
          sessionId
        }
    }
  }


}
