package resources

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.unmarshalling._
import akka.util.{ByteString}
import iptables.IpTablesService
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos._
import org.bitcoinj.protocols.payments.PaymentProtocol
import protocol.domain.Session
import wallet.WalletSupervisorService
import wallet.WalletSupervisorService._
import akka.pattern.ask
import ExtraHttpHeaders._
import commons.Helpers._

/**
  * Created by andrea on 15/09/16.
  */
trait PaymentChannelAPI extends CommonResource with ExtraDirectives {

  private[this] lazy val walletServiceActor = actorRefFor[WalletSupervisorService]

  private val iptables = new IpTablesService

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
      (walletServiceActor ? PAYMENT_REQUEST(session.id)).map { case req: PaymentRequest =>
        HttpEntity(req.toByteArray).withContentType(paymentRequestContentType)
      }
    }
  }

  private def paymentDataForSession(session:Session, offerId:String) = post {
    entity(as[Protos.Payment]){ payment =>
      complete {
        //Send the payment to the wallet actor and wait for its response
        (walletServiceActor ? PAYMENT(payment)).map { case PAYMENT_ACK =>
          HttpEntity(
            PaymentProtocol.createPaymentAck(payment, s"Enjoy session ${session.id}").toByteArray
          ).withContentType(paymentAckContentType)
        }
      }
    }
  }

  def enableMe(macAddress:String) = get {
    path("api" / "enableme") {
      complete(iptables.enableClient(macAddress))
    } ~ path("api" / "disableme") {
      complete(iptables.disableClient(macAddress))
    }
  }

  def paymentChannelRoute: Route = {
    path("api" / "pay" / Segment) { offerId:String =>
      sessionOrReject { session =>
        paymentRequestForSession(session, offerId) ~ paymentDataForSession(session, offerId)
      }
    }
  }

}
