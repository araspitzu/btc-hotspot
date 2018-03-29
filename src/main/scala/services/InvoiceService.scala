package services

import protocol.domain._
import registry.Registry


object InvoiceServiceRegistry extends Registry with InvoiceServiceComponent {

  override val invoiceService: InvoiceService = new InvoiceServiceImpl

}

trait InvoiceServiceComponent {

  val invoiceService: InvoiceService

}

trait InvoiceService {

    def invoiceFor(session: Session, offer: Offer): Invoice

}

class InvoiceServiceImpl extends InvoiceService {

  override def invoiceFor(session: Session, offer: Offer): Invoice = ???

}
