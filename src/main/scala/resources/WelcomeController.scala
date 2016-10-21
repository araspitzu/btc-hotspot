package resources

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.util.Timeout
import org.bitcoin.protocols.payments.Protos.PaymentRequest
import wallet.WalletSupervisorService
import wallet.WalletSupervisorService.{PAYMENT_REQUEST, GET_RECEIVING_ADDRESS}
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.ask
import ExtraHttpHeaders._
import commons.Helpers._

/**
  * Created by andrea on 15/09/16.
  */
trait WelcomeController extends CommonResource {

  implicit val timeout = Timeout(10 seconds)

  lazy val walletServiceActor = actorRefFor[WalletSupervisorService]


  def requestLogger:LoggingMagnet[HttpRequest ⇒ Unit] = LoggingMagnet { loggingAdapter => request =>
     loggingAdapter.info(s"Headers: ${request._3.toString()}")
  }

  def welcomeRoute: Route = {
    path("pay" / Segment) { sessionId:String =>
      get {
        logRequest(requestLogger){
          complete {
            (walletServiceActor ? PAYMENT_REQUEST(sessionId)).map { case req: PaymentRequest =>
              HttpEntity(req.toByteArray).withContentType(paymentRequestContentType)
            }
          }
        }
      } ~ post {
        complete {
          "Heyy"
        }
      }
    }

  }

  def getPaymentRequest(sessionId:String):Future[Array[Byte]] = {
    (walletServiceActor ? PAYMENT_REQUEST(sessionId)).map { case req: PaymentRequest =>
      req.toByteArray
    }
  }

}
