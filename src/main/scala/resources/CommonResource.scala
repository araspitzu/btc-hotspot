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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{ GenericMarshallers, Marshaller }
import akka.http.scaladsl.server.{ Directive1, Directives, Route }
import akka.stream.Materializer
import akka.util.Timeout
import protocol.domain.Session
import services.SessionService

import scala.compat.java8.OptionConverters._
import iptables.ArpService._

import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import commons.Helpers.FutureOption
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import commons.JsonSupport

import scala.concurrent.ExecutionContext

trait CommonResource extends Directives with Json4sSupport with JsonSupport with ExtraMarshallers {

  implicit val timeout = Timeout(10 seconds)

}

trait ExtraDirectives extends Directives with LazyLogging {

  val sessionService: SessionService

  def extractClientMAC: Directive1[Option[String]] = extractClientIP map { remoteAddress =>
    for {
      ipAddr <- remoteAddress.getAddress.asScala.map(_.getHostAddress)
      macAddr <- arpLookup(ipAddr)
    } yield macAddr
  }

  def withSession: Directive1[Option[Session]] = extractClientMAC map { someMac =>
    someMac map sessionService.byMacSync flatten //FIXME
  }

  def sessionOrReject: Directive1[Session] = withSession map {
    _ match {
      case None          => throw new IllegalArgumentException("Session not found") //FIXME
      case Some(session) => session
    }
  }

}

trait ExtraMarshallers extends GenericMarshallers {

  implicit def futureOptionMarshaller[A, B](implicit m: Marshaller[Option[A], B]): Marshaller[FutureOption[A], B] = {
    Marshaller(implicit ec => _.future.flatMap(m(_)))
  }

}

trait HttpApi extends LazyLogging {

  def bindOrFail(handler: Route, iface: String, port: Int, serviceName: String)(implicit system: ActorSystem, fm: Materializer, ec: ExecutionContext): Unit = {
    Http().bindAndHandle(handler, iface, port) map { binding =>
      logger.info(s"Service $serviceName bound to ${binding.localAddress}")
    } recover {
      case ex =>
        logger.error(s"Interface could not bind to $iface:$port", ex)
        throw ex
    }
  }
}