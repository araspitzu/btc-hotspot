package resources.miniportal

import resources.{ CommonResource, ExtraDirectives, ExtraMarshallers }
import services.{ InvoiceServiceInterface, InvoiceServiceImpl, SessionServiceImpl, SessionServiceInterface }
import wallet.WalletServiceInterface

trait InvoiceAPI extends CommonResource with ExtraDirectives with ExtraMarshallers {

  val sessionService: SessionServiceInterface
  val invoiceService: InvoiceServiceInterface
  val walletService: WalletServiceInterface

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
