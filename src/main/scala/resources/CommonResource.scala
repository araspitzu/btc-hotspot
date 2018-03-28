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

package resources

import akka.http.scaladsl.server.{ Directive, Directive1, Directives, Route }
import akka.util.Timeout
import protocol.domain.Session
import services.{ SessionServiceRegistry }

import scala.compat.java8.OptionConverters._
import iptables.ArpService._
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import akka.http.scaladsl.model._

trait CommonResource extends Directives with Json4sSupport with LazyLogging {

  implicit val timeout = Timeout(10 seconds)

}

object ExtraHttpHeaders {

  val paymentRequestContentType = ContentType.parse("application/bitcoin-paymentrequest").right.get
  val paymentAckContentType = ContentType.parse("application/bitcoin-paymentack").right.get

}

trait ExtraDirectives extends Directives with LazyLogging {

  def extractClientMAC: Directive1[Option[String]] = extractClientIP map { remoteAddress =>
    for {
      ipAddr <- remoteAddress.getAddress.asScala.map(_.getHostAddress)
      macAddr <- arpLookup(ipAddr)
    } yield macAddr
  }

  def withSession: Directive1[Option[Session]] = extractClientMAC map { someMac =>
    someMac map SessionServiceRegistry.sessionService.byMacSync flatten //FIXME
  }

  def sessionOrReject: Directive1[Session] = withSession map {
    _ match {
      case None          => throw new IllegalArgumentException("Session not found") //FIXME
      case Some(session) => session
    }
  }

}