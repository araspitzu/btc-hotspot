import akka.actor.{Actor, Props, ActorSystem}
import akka.http.scaladsl.Http
import commons.RestInterface
import ipc.{SharedStruct, SharedMemoryService}
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration._

object Boot extends App with RestInterface with LazyLogging {

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val actorSystem = ActorSystem("traffic-auth-actorSystem")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = actorSystem.dispatcher

  val theActor = actorSystem.actorOf(Props[SharedMemoryService], "sharedMemoryActor")

  val api = routes

  Http().bindAndHandle(handler = api, interface = host, port = port) map { binding =>
    logger.info(s"Interface bound to ${binding.localAddress}") } recover { case ex =>
    logger.info(s"Interface could not bind to $host:$port", ex.getMessage)
  }

}
