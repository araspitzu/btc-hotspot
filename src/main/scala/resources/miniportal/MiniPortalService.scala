package resources.miniportal

import akka.actor.ActorSystem
import akka.stream.Materializer
import commons.Configuration.MiniPortalConfig.{ miniPortalHost, miniPortalPort }
import resources.HttpApi
import services.{ InvoiceService, SessionService }
import wallet.WalletService

import scala.concurrent.ExecutionContext

class MiniPortalService(dependencies: {
  val sessionService: SessionService
  val invoiceService: InvoiceService
  val walletService: WalletService
})(implicit actorSystem: ActorSystem, fm: Materializer, executor: ExecutionContext) extends MiniPortal with HttpApi {

  val ec = executor

  val sessionService: SessionService = dependencies.sessionService
  val invoiceService: InvoiceService = dependencies.invoiceService
  val walletService: WalletService = dependencies.walletService

  bindOrFail(miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")

}
