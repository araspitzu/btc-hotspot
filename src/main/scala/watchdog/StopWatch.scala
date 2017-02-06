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

package watchdog

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.typesafe.scalalogging.slf4j.LazyLogging
import protocol.domain.{Offer, QtyUnit, Session}
import protocol.domain.QtyUnit._
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import iptables.IpTablesInterface
import protocol.SessionRepositoryImpl
import registry.{IpTablesServiceRegistry, SchedulerRegistry, SessionRepositoryRegistry}

import scala.concurrent.Future
import scala.concurrent.duration._

object StopWatch {
  
  val env = new {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val scheduler: SchedulerImpl = SchedulerRegistry.schedulerImpl
    val ipTableFun = IpTablesServiceRegistry.ipTablesServiceImpl
  }

  
  def forOffer(session: Session, offer:Offer):StopWatch = offer.qtyUnit match {
    case MB => ???
    case minutes => new TimebasedStopWatch(env, session, offer)
  }
  
}



trait StopWatch extends LazyLogging {
  
  val session:Session
  val offer:Offer
  
  def start():Future[Option[Long]]
  
  def stop():Unit
  
  def remainingUnits():Long
  
  def isActive():Boolean

  def onLimitReach():Unit
  
}

class TimebasedStopWatch(dependencies:{
   val sessionRepository: SessionRepositoryImpl
   val scheduler: SchedulerImpl
   val ipTableFun: IpTablesInterface
}, val session: Session, val offer: Offer) extends StopWatch {
  
  import dependencies._
  
  require(offer.qtyUnit == QtyUnit.millis, s"Time based stopwatch can only be used with offers in milliseconds, offer id ${offer.offerId}")
  
  override def start(): Future[Option[Long]] = {
    logger.info(s"Starting session ${session.id}")
    for {
     ipTablesOut <- ipTableFun.enableClient(session.clientMac)                              //alter iptables
     remainingMillis = if(session.remainingUnits < 0) offer.qty else session.remainingUnits //select remaining units
     _ = scheduler.schedule(session.id, remainingMillis millisecond) {                      //start countdown
       this.onLimitReach()
     }
     upsertSessionId <- sessionRepository.upsert(session.copy(remainingUnits = remainingMillis)).future  //update remaining time in session
    } yield {
      logger.info(s"Upserted session is $upsertSessionId")
      upsertSessionId
    }
    
  }
  
  override def stop(): Unit = {
    logger.info(s"Stopping ${session.id}")
    
    // alter iptables
    ipTableFun.disableClient(session.clientMac)
    
    // abort scheduled task
    if (isActive) scheduler.cancel(session.id)
    
    // update remaining time in session WAIT FOR FUTURE?
    sessionRepository.upsert(session.copy(
      remainingUnits = this.remainingUnits()
    ))
    
  }
  
  override def remainingUnits(): Long = {
    scheduler.scheduledAt(session.id) match {
      case Some(scheduledAt) => ChronoUnit.MILLIS.between(LocalDateTime.now, scheduledAt)
      case None => throw new IllegalArgumentException(s"Could not find schedule for $session")
    }
  }
  
  override def onLimitReach(): Unit = {
    logger.info(s"Reached offer limit for session ${session.id}")
    this.stop()
  }
  
  override def isActive() = scheduler.isScheduled(session.id)
  
}
//class DatabasedStopWatch extends StopWatch