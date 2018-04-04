package resources.miniportal

import commons.JsonSupport
import resources.{ CommonResource, ExtraDirectives }
import wallet.WalletServiceRegistry

trait InvoiceAPI extends CommonResource with ExtraDirectives {

  def invoiceRoute = get {
    sessionOrReject { session =>
      pathPrefix("api" / "invoice") {
        path("offer" / LongNumber) { offerId =>
          complete(WalletServiceRegistry.walletService.generateInvoice(session, offerId))
        } ~ path(Segment) { asd =>
          complete("true")
        }
      }

    }
  }

}
