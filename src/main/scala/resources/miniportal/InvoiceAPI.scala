package resources.miniportal

import resources.{ CommonResource, ExtraDirectives, ExtraMarshallers }
import services.InvoiceServiceRegistry.invoiceService
import services.SessionServiceImpl
import wallet.WalletServiceRegistry.walletService

trait InvoiceAPI extends CommonResource with ExtraDirectives with ExtraMarshallers {

  val sessionService: SessionServiceImpl

  def invoiceRoute = get {
    pathPrefix("api" / "invoice" / LongNumber) { invoiceId =>
      sessionOrReject { session =>
        pathEnd {
          complete(invoiceService.invoiceById(invoiceId))
        } ~ path("paid") {
          complete(walletService.checkInvoicePaid(invoiceId))
        } ~ path("actualize") {
          complete(sessionService.enableSessionForInvoice(session, invoiceId))
        }
      }
    }
  }

}
