package resources

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import akka.util.{ByteString}
import commons.{AppExecutionContextRegistry, AppExecutionContext}
import iptables.IpTablesService._
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos._
import protocol.domain.Session
import wallet.WalletServiceComponent
import ExtraHttpHeaders._
import AppExecutionContextRegistry.context._

/**
  * Created by andrea on 15/09/16.
  */
trait PaymentChannelAPI extends CommonResource with ExtraDirectives {
  this:WalletServiceComponent  =>

  def headerLogger:LoggingMagnet[HttpRequest ⇒ Unit] = LoggingMagnet { loggingAdapter => request =>
     loggingAdapter.debug(s"Headers: ${request._3.toString()}")
     loggingAdapter.debug(s"HTTP Method: ${request._1}")
  }


  implicit val paymentUnmarshaller:FromRequestUnmarshaller[Protos.Payment] = Unmarshaller { ec => httpRequest =>
    httpRequest._4.dataBytes.runFold(ByteString.empty)(_ ++ _) map { byteString =>
      Protos.Payment.parseFrom(byteString.toArray[Byte])
    }
  }

  private def paymentRequestForSession(session:Session, offerId:String) = get {
    complete {
      walletService.generatePaymentRequest(session, offerId) map { req:PaymentRequest =>
        HttpEntity(req.toByteArray).withContentType(paymentRequestContentType)
      }
    }
  }

  private def paymentDataForSession(session:Session, offerId:String) = post {
    entity(as[Protos.Payment]){ payment =>
      complete {
        walletService.validatePayment(payment) map { ack =>
          HttpEntity(ack.toByteArray).withContentType(paymentAckContentType)
        }
      }
    }
  }

  def enableMe(macAddress:String) = get {
    path("api" / "enableme") {
      complete(enableClient(macAddress))
    } ~ path("api" / "disableme") {
      complete(disableClient(macAddress))
    }
  }

  def paymentChannelRoute: Route = {
    path("api" / "pay" / Segment) { offerId =>
      sessionOrReject { session =>
        paymentRequestForSession(session, offerId) ~ paymentDataForSession(session, offerId)
      }
    }
  }

}
