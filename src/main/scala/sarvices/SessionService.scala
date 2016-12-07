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

package sarvices

import com.typesafe.scalalogging.slf4j.LazyLogging
import protocol.Repository.SessionRepository._
import protocol.domain.{Offer, Session}
import commons.AppExecutionContextRegistry.context._
import iptables.IpTablesService
import watchdog.Scheduler

import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}
import scala.concurrent.{Await, Future}

/**
  * Created by andrea on 27/11/16.
  */
object SessionService extends LazyLogging {
  
  def enableSessionFor(session: Session, offer:Offer) = {
        
    for {
      clientEnabled <- IpTablesService.enableClient(session.clientMac)
      sessionUpdated <- updateSessionWithOffer(session, Some(offer))
    } yield {
      logger.info(s"Enabled ${session.clientMac} for offer ${offer.offerId}")
      val offerRemainingTime = FiniteDuration(offer.qty, SECONDS)
      Scheduler.schedule(session.id, offerRemainingTime) {
        IpTablesService.disableClient(session.clientMac)
      }
      
    }
  
  }
  
  def disableSession(session: Session) = {
    Scheduler.tasks.get(session.id) match {
      case None => ???
      case Some(scheduledTask) => updateSessionWithOffer(session, None) map { _ =>
        
        Scheduler.tasks.get(session.id) match {
          case None => ()
          case Some(scheduled) =>
            scheduled.cancellable.cancel
            //remove from IpTables?
        }
        
  
      }
    }
  }

  def byMac(mac: String): Future[Option[Session]] = byMacAddress(mac)

  //TODO fucking remove!
  def byMacSync(mac: String): Option[Session] = {
    Await.result(byMac(mac), Duration(10, "seconds"))
  }

  /*
    Returns the id of the existing session for this mac, create a new one if
    no session can be found, ids are created from the database
   */
  def getOrCreate(mac:String):Future[Long] = {
    byMac(mac) flatMap {
      case Some(session) =>
        Future.successful(session.id)
      case None => insert(Session(clientMac = mac)) map { sessionId =>
          logger.info(s"Created session $sessionId for $mac")
          sessionId
        }
    }
  }


}
