package resources

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.util.Timeout
import org.bitcoin.protocols.payments.Protos.PaymentRequest
import wallet.WalletSupervisorService
import wallet.WalletSupervisorService.{PAYMENT_REQUEST, GET_RECEIVING_ADDRESS}
import scala.concurrent.duration._
import akka.pattern.ask
import commons.Configuration._

/**
  * Created by andrea on 15/09/16.
  */
trait WelcomeController extends CommonResource {

  implicit val timeout = Timeout(10 seconds)

  def walletActorPath = s"${config.getString("akka.actorSystem")}/user/${WalletSupervisorService.getClass.getSimpleName}"
  lazy val walletServiceActor = actorSystem.actorSelection(s"akka://$walletActorPath")

  def requestLogger:LoggingMagnet[HttpRequest ⇒ Unit] = LoggingMagnet { loggingAdapter => request =>
     loggingAdapter.info(s"Headers: ${request._3.toString()}")
  }

  def welcomeRoute: Route = {
    path("pay" / Segment) { sessionId:String =>
      get {
        logRequest(requestLogger){
          //set Content-Type: application/bitcoin-paymentrequest
          //set Content-Transfer-Encoding: binary

          complete {
            (walletServiceActor ? PAYMENT_REQUEST(sessionId)).map { case req: PaymentRequest =>

              req.toByteArray
            }
          }
        }
      } ~ post {
        // Receive bitcoin payment
        complete {
          "Heyy"
        }
      }
    }

  }

}
