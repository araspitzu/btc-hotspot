package resources.miniportal

import resources.{ CommonResource, ExtraDirectives, ExtraMarshallers }
import services.{ InvoiceService, InvoiceServiceImpl, SessionServiceImpl, SessionService }
import wallet.WalletService

trait InvoiceAPI extends CommonResource with ExtraDirectives with ExtraMarshallers {

  val sessionService: SessionService
  val invoiceService: InvoiceService
  val walletService: WalletService

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
