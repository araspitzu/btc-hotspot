import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration.MiniPortalConfig._
import resources.{PaymentChannelAPI, MiniPortal}
import wallet.WalletServiceComponent
import commons.AppExecutionContextRegistry.context._

object MiniPortalRegistry
  extends MiniPortal
    with PaymentChannelAPI
    with WalletServiceComponent {

  override val walletService = new WalletService
}

object Boot extends App with LazyLogging {
  logger.info(s"Starting paypercom-hotspot")

  bindOrFail(MiniPortalRegistry.miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")


  def bindOrFail(handler:Route, iface:String, port:Int, serviceName:String):Unit = {
    Http().bindAndHandle(handler, iface, port) map { binding =>
      logger.info(s"Service $serviceName bound to ${binding.localAddress}") } recover { case ex =>
      logger.info(s"Interface could not bind to $iface:$port", ex.getMessage)
      throw ex;
    }
  }

}


