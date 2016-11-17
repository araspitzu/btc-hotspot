import akka.actor.{ Props, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration._
import resources.{StaticFiles, MiniPortal}
import wallet.WalletSupervisorService
import commons.Configuration.MiniPortalConfig._

object Boot extends App
  with MiniPortal
  with LazyLogging {

  implicit val actorSystem = ActorSystem(config.getString("akka.actorSystem"))
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = actorSystem.dispatcher

  bindOrFail(miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")

  //Spawn wallet service actor
  actorSystem.actorOf(Props[WalletSupervisorService],WalletSupervisorService.getClass.getSimpleName)


  def bindOrFail(handler:Route, iface:String, port:Int, serviceName:String):Unit = {
    Http().bindAndHandle(handler, iface, port) map { binding =>
      logger.info(s"Service $serviceName bound to ${binding.localAddress}") } recover { case ex =>
      logger.info(s"Interface could not bind to $iface:$port", ex.getMessage)
      throw ex;
    }
  }

}


