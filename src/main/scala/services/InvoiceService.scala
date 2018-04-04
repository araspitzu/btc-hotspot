package services

import protocol.{ InvoiceRepository, InvoiceRepositoryImpl, SessionRepositoryComponent, SessionRepositoryImpl }
import protocol.domain._
import registry.{ InvoiceRepositoryRegistry, Registry }
import wallet.WalletServiceInterface

object InvoiceServiceRegistry extends Registry with InvoiceServiceComponent {

  override val invoiceService: InvoiceService = new InvoiceServiceImpl

}

trait InvoiceServiceComponent {

  val invoiceService: InvoiceService

}

trait InvoiceService {

  def invoiceFor(session: Session, offer: Offer): Invoice

}

class InvoiceServiceImpl(dependencies: {
  val invoiceRepository: InvoiceRepository
}) extends InvoiceService {

  private def invoiceRepository = dependencies.invoiceRepository.invoiceRepository

  def this() = this(new {
    val invoiceRepository: InvoiceRepository = InvoiceRepositoryRegistry
  })

  override def invoiceFor(session: Session, offer: Offer) = {
    ???
  }

}
