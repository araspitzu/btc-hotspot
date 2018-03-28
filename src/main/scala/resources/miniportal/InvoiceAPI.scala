package resources.miniportal

import commons.JsonSupport
import resources.{ CommonResource, ExtraDirectives }
import wallet.WalletServiceRegistry

trait InvoiceAPI extends CommonResource with ExtraDirectives with JsonSupport {

  def invoiceRoute = get {
    withSession { session =>
      pathPrefix("api" / "invoice") {
        path("offer" / LongNumber) { offerId =>
          complete(WalletServiceRegistry.walletService.generateInvoice(session.get, offerId))
        } ~ path(Segment) {
          complete("true")
        }
      }

    }
  }

}
