package resources.miniportal

import commons.Configuration.MiniPortalConfig.{ miniPortalHost, miniPortalPort }
import resources.HttpApi
import services.{ InvoiceService, SessionService }
import wallet.WalletService

class MiniPortalService(dependencies: {
  val sessionService: SessionService
  val invoiceService: InvoiceService
  val walletService: WalletService
}) extends MiniPortal with HttpApi {

  val sessionService: SessionService = dependencies.sessionService
  val invoiceService: InvoiceService = dependencies.invoiceService
  val walletService: WalletService = dependencies.walletService

  bindOrFail(miniportalRoute, miniPortalHost, miniPortalPort, "MiniPortal")

}
