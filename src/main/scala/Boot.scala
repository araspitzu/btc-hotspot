import akka.actor.{Actor, Props, ActorSystem}
import akka.http.scaladsl.Http
import commons.RestInterface
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration._
import wallet.WalletSupervisorService

object Boot extends App with RestInterface with LazyLogging {

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val actorSystem = ActorSystem("traffic-auth-actorSystem")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = actorSystem.dispatcher

  val api = routes

  Http().bindAndHandle(handler = api, interface = host, port = port) map { binding =>
    logger.info(s"Interface bound to ${binding.localAddress}") } recover { case ex =>
    logger.info(s"Interface could not bind to $host:$port", ex.getMessage)
  }

  val walletServiceActor = actorSystem.actorOf(Props[WalletSupervisorService],"WalletSupervisorService")



}
