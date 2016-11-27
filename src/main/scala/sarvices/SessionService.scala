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
import protocol.domain.Session
import commons.AppExecutionContextRegistry.context._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by andrea on 27/11/16.
  */
object SessionService extends LazyLogging {

  val futureTimeoutDuration = Duration(10, "seconds")

  def byMac(mac: String): Future[Option[Session]] = {
    SessionRepository.byMacAddress(mac)
  }

  def byMacSync(mac: String): Option[Session] = {
    Await.result(byMac(mac), futureTimeoutDuration)
  }

//  def createIfNotExist(mac:String) = {
//    byMac(mac).map {
//      case
//    }
//  }

  def create(mac: String): Future[String] = {
    val session = Session(clientMac = mac)
    SessionRepository.insert(session)

    //        logger.info(s"Found exising session: ${existingSession.id} for $mac")
  }
}
