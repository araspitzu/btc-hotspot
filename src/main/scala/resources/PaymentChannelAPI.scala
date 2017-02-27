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

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.unmarshalling._
import akka.util.ByteString
import commons.AppExecutionContextRegistry
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos._
import protocol.domain.Session
import ExtraHttpHeaders._
import AppExecutionContextRegistry.context._
import registry.IpTablesServiceRegistry
import services.SessionServiceRegistry
import wallet.WalletServiceRegistry


trait PaymentChannelAPI extends CommonResource with ExtraDirectives {
  
  def sessionService = SessionServiceRegistry.sessionService

  def headerLogger:LoggingMagnet[HttpRequest ⇒ Unit] = LoggingMagnet { loggingAdapter => request =>
     loggingAdapter.debug(s"Headers: ${request._3.toString()}")
     loggingAdapter.debug(s"HTTP Method: ${request._1}")
  }


  implicit val paymentUnmarshaller:FromRequestUnmarshaller[Protos.Payment] = Unmarshaller { ec => httpRequest =>
    httpRequest._4.dataBytes.runFold(ByteString.empty)(_ ++ _) map { byteString =>
      Protos.Payment.parseFrom(byteString.toArray[Byte])
    }
  }

  private def paymentRequestForSession(session:Session, offerId:Long) = get {
    complete {
      WalletServiceRegistry.walletService.generatePaymentRequest(session, offerId) map { req:PaymentRequest =>
        HttpEntity(req.toByteArray).withContentType(paymentRequestContentType)
      }
    }
  }

  private def paymentDataForSession(session:Session, offerId:Long) = post {
    entity(as[Protos.Payment]){ payment =>
      complete {
        sessionService.payAndEnableSessionForOffer(session, offerId, payment).future.map(_.get).map { ack =>
           HttpEntity(ack.toByteArray).withContentType(paymentAckContentType)
        }
      }
    }
  }

  def enableMeRoute = extractClientMAC {
    _ match {
      case Some(mac) => enableMe(mac)
      case None => reject(ValidationRejection("Mac not found"))
    }
  }

  def enableMe(macAddress:String) = get {
    path("api" / "enableme") {
      complete(IpTablesServiceRegistry.ipTablesServiceImpl.enableClient(macAddress).future)
    } ~ path("api" / "disableme") {
      complete(IpTablesServiceRegistry.ipTablesServiceImpl.disableClient(macAddress).future)
    }
  }

  def paymentChannelRoute: Route = {
    path("api" / "pay" / LongNumber) { offerId =>
      sessionOrReject { session =>
        paymentRequestForSession(session, offerId) ~
        paymentDataForSession(session, offerId)
      }
    }
  }

}
