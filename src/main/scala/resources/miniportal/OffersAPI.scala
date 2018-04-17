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

package resources.miniportal

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import resources.{ CommonResource, ExtraDirectives }
import services.InvoiceServiceImpl

import scala.concurrent.duration._
import scala.concurrent.Await

trait OffersAPI extends CommonResource with ExtraDirectives with LazyLogging {

  val invoiceService: InvoiceServiceImpl

  def offersRoute: Route = get {
    pathPrefix("api" / "offer" / LongNumber) { offerId =>
      path("buy") {
        sessionOrReject { session =>
          redirect(s"/invoice.html?invoiceId=${Await.result(invoiceService.makeNewInvoice(session, offerId), 5 seconds)}", TemporaryRedirect)
        }
      } ~ pathEndOrSingleSlash {
        complete(invoiceService.offerById(offerId))
      }
    } ~ path("api" / "offer") {
      complete(invoiceService.allOffers)
    }
  }

}
