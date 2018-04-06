package resources.miniportal

import resources.{ CommonResource, ExtraDirectives, ExtraMarshallers }
import services.InvoiceServiceRegistry
import wallet.WalletServiceRegistry

trait InvoiceAPI extends CommonResource with ExtraDirectives with ExtraMarshallers {

  def walletService = WalletServiceRegistry.walletService
  def invoiceService = InvoiceServiceRegistry.invoiceService

  def invoiceRoute = get {
    pathPrefix("api" / "invoice" / LongNumber) { invoiceId =>
      sessionOrReject { session =>
        pathEnd {
          complete(invoiceService.invoiceById(invoiceId))
        } ~ path("paid") {
          complete(walletService.checkInvoicePaid(invoiceId))
        }
      }
    }
  }

}
