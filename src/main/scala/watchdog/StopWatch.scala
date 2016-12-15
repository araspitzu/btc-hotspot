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
import java.util.concurrent.TimeoutException

import com.typesafe.scalalogging.slf4j.LazyLogging
import iptables.IpTablesService
import protocol.SessionRepository
import protocol.domain.{Offer, Session}
import protocol.domain.QtyUnit._
import commons.AppExecutionContextRegistry.context._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Created by andrea on 07/12/16.
  */
object StopWatch {
  
  def forOffer(session: Session, offer:Offer):StopWatch = offer.qtyUnit match {
    case MB => ???
    case minutes => TimebasedStopWatch(session, offer)
  }
  
}

sealed trait StopWatch extends LazyLogging {
  
  val session:Session
  val offer:Offer
  
  def start:Unit
  
  def stop:Unit
  
  def remainingTime:Long
  
  def onLimitReach:Unit
  
}

case class TimebasedStopWatch(session: Session, offer: Offer) extends StopWatch {
  
  override def start: Unit = {
    val remainingMillis = if(session.remainingUnits < 0) offer.qty else session.remainingUnits
    
    //alter iptables
    Try {
      Await.result(IpTablesService.enableClient(session.clientMac), 2 seconds)
    } match {
      case Success(iptablesOutput) => ()
      case Failure(thr) => thr match {
        case err:TimeoutException => throw new TimeoutException("Timeout doing iptables op")
        case err => throw err
      }
    }
    
    
    //start countdown
    Scheduler.schedule(session.id, Duration(remainingMillis, "seconds")) {
      this.onLimitReach
    }
    
    //update remaining time in session
    SessionRepository.upsert(session.copy(remainingUnits = remainingMillis))
  }
  
  override def stop: Unit = {
    val remainingMillis:Long = this.remainingTime
    
    // alter iptables
    IpTablesService.disableClient(session.clientMac)
    
    // abort scheduled task
    Scheduler.cancel(session.id)
    
    // update remaining time in session WAIT FOR FUTURE?
    SessionRepository.upsert(session.copy(
      remainingUnits = remainingMillis
    ))
    
  }
  
  override def remainingTime: Long = {
    Scheduler.scheduledAt(session.id) match {
      case Some(scheduledAt) => ChronoUnit.MILLIS.between(scheduledAt, LocalDateTime.now)
      case None => throw new IllegalArgumentException(s"Could not find schedule for $session")
    }
  }
  
  override def onLimitReach: Unit = {
    logger.info(s"Reached offer limit for session ${session.id}")
    IpTablesService.disableClient(session.clientMac)
  }
  
}
//class DatabasedStopWatch extends StopWatch