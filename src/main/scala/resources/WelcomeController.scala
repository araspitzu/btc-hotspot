package resources

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server._
import akka.util.Timeout
import wallet.WalletSupervisorService.GET_RECEIVING_ADDRESS
import scala.concurrent.duration._
import akka.pattern.ask
import akka.http.scaladsl.model.ContentTypes.`text/html(UTF-8)`

/**
  * Created by andrea on 15/09/16.
  */
trait WelcomeController extends CommonResource {

  implicit val timeout = Timeout(10 seconds)

  def route: Route = {
    get {
      path("welcome"){
        complete {
          HttpEntity(`text/html(UTF-8)`,  greetingPage)
        }
      } ~ path("address") {
        complete {
          ask(
            actorSystem.actorSelection("akka://traffic-auth-actorSystem/user/WalletSupervisorService"),
            GET_RECEIVING_ADDRESS
          ).mapTo[String]
        }
      }
    }
  }

  val greetingPage =
    """
      |<HTML>
      |<HEAD>
      |<TITLE> Welcome :)</TITLE>
      |</HEAD>
      |<BODY BGCOLOR="FFFFFF">
      |
      |<HR>
      |<H1>This is a Header</H1>
      |<H2>This is a Medium Header</H2>
      |Send me mail at <a href="mailto:support@yourcompany.com">
      |support@yourcompany.com</a>.
      |<P> This is a new paragraph!
      |<P> <B>This is a new paragraph!</B>
      |<BR> <B><I>This is a new sentence without a paragraph break, in bold italics.</I></B>
      |<HR>
      |</BODY>
      |</HTML>
    """.stripMargin

}
