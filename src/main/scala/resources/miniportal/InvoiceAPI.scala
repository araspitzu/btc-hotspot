package resources.miniportal

import resources.{ CommonResource, ExtraDirectives, ExtraMarshallers }
import services.{ InvoiceServiceImpl, SessionServiceImpl }
import wallet.WalletServiceRegistry.walletService

trait InvoiceAPI extends CommonResource with ExtraDirectives with ExtraMarshallers {

  val sessionService: SessionServiceImpl
  val invoiceService: InvoiceServiceImpl

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
