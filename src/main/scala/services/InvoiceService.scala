package services

import com.typesafe.scalalogging.LazyLogging
import commons.Helpers.FutureOption
import ln.{ EclairClient, EclairClientComponent, EclairClientRegistry }
import protocol.{ InvoiceRepositoryComponent, OfferRepositoryComponent }
import protocol.domain.{ Invoice, Offer, Session }
import registry.{ InvoiceRepositoryRegistry, OfferRepositoryRegistry, Registry }
import commons.AppExecutionContextRegistry.context._
import scala.concurrent.Future

object InvoiceServiceRegistry extends Registry with InvoiceServiceComponent {

  override val invoiceService: InvoiceService = new InvoiceServiceImpl

}

trait InvoiceServiceComponent {

  val invoiceService: InvoiceService
}

trait InvoiceService {

  def makeNewInvoice(session: Session, offerId: Long): Future[Long]

  def invoiceById(invoiceId: Long): FutureOption[Invoice]

  def allOffers: Future[Seq[Offer]]

  def offerById(id: Long): FutureOption[Offer]

}

class InvoiceServiceImpl(dependencies: {
  val invoiceRepositoryComponent: InvoiceRepositoryComponent
  val offerRepositoryComponent: OfferRepositoryComponent
  val eclairClientComponent: EclairClientComponent
}) extends InvoiceService with LazyLogging {

  def this() = this(new {
    val invoiceRepositoryComponent = InvoiceRepositoryRegistry
    val offerRepositoryComponent = OfferRepositoryRegistry
    val eclairClientComponent = EclairClientRegistry
  })

  private def invoiceRepository = dependencies.invoiceRepositoryComponent.invoiceRepositoryImpl
  private def offerRepository = dependencies.offerRepositoryComponent.offerRepositoryImpl
  private def eclairClient = dependencies.eclairClientComponent.eclairClient

  override def makeNewInvoice(session: Session, offerId: Long): Future[Long] = {
    logger.info(s"Creating new invoice for session: ${session.id}, offer:$offerId")
    for {
      offer <- offerRepository.byId(offerId) orFailWith s"Offer $offerId not found"
      invoiceMsg = s"Please pay ${offer.price} satoshis for ${offer.description}, MAC:${session.clientMac}"
      eclairResponse <- eclairClient.getInvoice(offer.price, invoiceMsg)
      invoice = Invoice(paid = false, lnInvoice = eclairResponse, sessionId = Some(session.id), offerId = Some(offerId))
      invoiceId <- invoiceRepository.insert(invoice)
    } yield {
      logger.info(s"New invoice id=$invoiceId with data:\n"+
        s"Expiration date:${invoice.expiresAt}"+
        s"Price: ${offer.price}"
      )

      invoiceId
    }

  }

  override def invoiceById(invoiceId: Long) = invoiceRepository.invoiceById(invoiceId)

  override def allOffers = offerRepository.allOffers

  override def offerById(id: Long) = offerRepository.byId(id)
}
