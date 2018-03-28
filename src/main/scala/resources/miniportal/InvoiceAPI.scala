package resources.miniportal

import commons.JsonSupport
import resources.{ CommonResource, ExtraDirectives }
import wallet.WalletServiceRegistry

trait InvoiceAPI extends CommonResource with ExtraDirectives with JsonSupport {

  def invoiceRoute = get {
    withSession { session =>
      path("api" / "invoice" / LongNumber) { offerId =>
        complete(WalletServiceRegistry.walletService.generateInvoice(session.get, offerId))
      }
    }
  }

}
