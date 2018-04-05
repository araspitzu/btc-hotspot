package resources.miniportal

import resources.{ CommonResource, ExtraDirectives, ExtraMarshallers }
import wallet.WalletServiceRegistry

trait InvoiceAPI extends CommonResource with ExtraDirectives with ExtraMarshallers {

  def walletService = WalletServiceRegistry.walletService

  def invoiceRoute = get {
    pathPrefix("api" / "invoice") {
      sessionOrReject { session =>

        path("offer" / LongNumber) { offerId =>
          complete(walletService.generateInvoice(session, offerId))
        } ~ path(LongNumber / "paid") { invoiceId =>
          complete(walletService.checkInvoicePaid(invoiceId))
        }

      }

    }
  }

}
