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

import com.typesafe.scalalogging.slf4j.LazyLogging
import protocol.domain.{Offer, Session}
import protocol.SessionRepository._
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by andrea on 27/11/16.
  */
object SessionService extends LazyLogging {
  
  def enableSessionFor(session: Session, offer:Offer):FutureOption[Unit] = {
        
    for {
      sessionWithOffer <- updateSessionWithOffer(session, offer)
    } yield {
      logger.info(s"Enabling session ${session.id} for offer ${session.offerId}")
      sessionWithOffer.start
    }
  
  }
  
  def disableSession(session: Session) = {
    session.stop
  }

  def byId(id:Long):FutureOption[Session] = bySessionId(id)
  
  def byMac(mac: String): FutureOption[Session] = byMacAddress(mac)

  //TODO fucking remove!
  def byMacSync(mac: String): Option[Session] = {
    Await.result(byMac(mac).future, 10 seconds)
  }

  /*
    Returns the id of the existing session for this mac, create a new one if
    no session can be found, ids are created from the database
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
