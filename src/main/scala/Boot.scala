import akka.actor.{Actor, Props, ActorSystem}
import akka.http.scaladsl.Http
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.util.Timeout

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging

object Boot extends App with RestInterface with LazyLogging {

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem("traffic-auth-system")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher
  //implicit val timeout = Timeout(10 seconds)

  val api = routes

  Http().bindAndHandle(handler = api, interface = host, port = port) map { binding =>
    println(s"Interface bound to ${binding.localAddress}") } recover { case ex =>
    println(s"Interface could not bind to $host:$port", ex.getMessage)
  }

}
