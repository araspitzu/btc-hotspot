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

import akka.http.scaladsl.model.Uri.{Query, Path}
import akka.http.scaladsl.server.{Directive1, Directives}
import akka.util.Timeout
import protocol.domain.Session
import sarvices.SessionService
import scala.compat.java8.OptionConverters._
import iptables.ArpService._
import scala.concurrent.duration._
import com.typesafe.scalalogging.slf4j.LazyLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import akka.http.scaladsl.model._
import commons.Configuration.MiniPortalConfig._

/**
  * Created by andrea on 09/09/16.
  */
trait CommonResource extends Directives with Json4sSupport with LazyLogging {

  implicit val timeout = Timeout(10 seconds)

}

object ExtraHttpHeaders {

  val paymentRequestContentType: ContentType = contentTypeFor("application/bitcoin-paymentrequest")
  val paymentAckContentType: ContentType = contentTypeFor("application/bitcoin-paymentack")

  private def contentTypeFor(customContentType:String) = ContentType.parse(customContentType) match {
    case Right(contentType) => contentType
    case Left(err) => throw new RuntimeException(s"Unable to generate Content-Type for $customContentType, ${err.toString}")
  }

}

trait ExtraDirectives extends Directives {

  def extractClientMAC:Directive1[Option[String]] = extractClientIP map { remoteAddress =>
    for {
      ipAddr <- remoteAddress.getAddress.asScala.map(_.getHostAddress)
      macAddr <- arpLookup(ipAddr)
    } yield macAddr
  }

  def extractSessionForMac:Directive1[Option[Session]] = extractClientMAC map { someMac =>
    someMac map SessionService.byMacSync flatten
  }

  def redirectToPrelogin(request: Option[HttpRequest] = None) =
    redirect(preLoginUrl(request), StatusCodes.TemporaryRedirect)

  def sessionOrReject:Directive1[Session] = extractSessionForMac map {
    _ match {
      case None => throw new IllegalArgumentException("Session not found")
      case Some(session) => session
    }
  }

  private def preLoginUrl(request: Option[HttpRequest]):Uri = {
    Uri()
    .withScheme("http")
    .withHost(miniPortalHost)
    .withPort(miniPortalPort)
    .withPath(Path("/prelogin"))
    .withQuery(Query(
      "userUrl" -> request.map(_._2.toString).getOrElse("duckduckgo.com")
    ))
  }

}