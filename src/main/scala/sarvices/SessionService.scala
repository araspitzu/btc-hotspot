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
import protocol.Repository.SessionRepository
import protocol.domain.{Offer, Session}
import commons.AppExecutionContextRegistry.context._
import iptables.IpTablesService
import protocol.domain.QtyUnit.QtyUnit
import protocol.domain.QtyUnit._

import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}
import scala.concurrent.{Await, Future}

/**
  * Created by andrea on 27/11/16.
  */
object SessionService extends LazyLogging {

  val futureTimeoutDuration = Duration(10, "seconds")
  
  def enableSessionFor(session: Session, offer:Offer) = {
    if(offer.qtyUnit == MB)
      throw new NotImplementedError
  
    val offerRemainingTime = FiniteDuration(offer.qty, SECONDS)
    IpTablesService.enableClient(session.clientMac)
    logger.info(s"Enabled ${session.clientMac}, scheduled disable for now + $offerRemainingTime")
    actorSystem.scheduler.scheduleOnce(offerRemainingTime) {
      IpTablesService.disableClient(session.clientMac)
    }
  
  }

  def byMac(mac: String): Future[Option[Session]] = {
    SessionRepository.byMacAddress(mac)
  }

  def byMacSync(mac: String): Option[Session] = {
    Await.result(byMac(mac), futureTimeoutDuration)
  }

  /*
    Returns the id of the existing session for this mac, create a new one if
    no session can be found, ids are created from the database
   */
  def getOrCreate(mac:String):Future[Long] = {
    byMacSync(mac) match {
      case Some(session) =>
        Future.successful(session.id)
      case None =>
        val session = Session(clientMac = mac)
        SessionRepository.insert(session) map { sessionId =>
          logger.info(s"Created session $sessionId for $mac")
          sessionId
        }
    }
  }


}
