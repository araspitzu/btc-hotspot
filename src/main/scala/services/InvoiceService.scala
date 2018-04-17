package services

import com.typesafe.scalalogging.LazyLogging
import commons.Helpers.FutureOption
import scala.concurrent.duration._
import ln.{ EclairClient, EclairClientComponent, EclairClientRegistry }
import protocol.{ InvoiceRepositoryComponent, OfferRepositoryComponent, webDto }
import protocol.webDto._
import protocol.domain.{ Invoice, Offer, Session }
import registry.{ OfferRepositoryRegistry, Registry }
import commons.AppExecutionContextRegistry.context._

import scala.concurrent.{ Await, Future }

object InvoiceServiceRegistry extends Registry with InvoiceServiceComponent {

  override val invoiceService: InvoiceService = new InvoiceServiceImpl(???)

}

trait InvoiceServiceComponent {

  val invoiceService: InvoiceService
}

trait InvoiceService {

  def makeNewInvoice(session: Session, offerId: Long): Future[Long]

  def invoiceById(invoiceId: Long): FutureOption[InvoiceDto]

  def allOffers: Future[Seq[Offer]]

  def offerById(id: Long): FutureOption[Offer]

}

class InvoiceServiceImpl(dependencies: {
  val invoiceRepositoryComponent: InvoiceRepositoryComponent
  val offerRepositoryComponent: OfferRepositoryComponent
  val eclairClientComponent: EclairClientComponent
}) extends InvoiceService with LazyLogging {

  //  def this() = this(new {
  //    val invoiceRepositoryComponent = ???
  //    val offerRepositoryComponent = OfferRepositoryRegistry
  //    val eclairClientComponent = EclairClientRegistry
  //  })

  private def invoiceRepository = dependencies.invoiceRepositoryComponent.invoiceRepositoryImpl
  private def offerRepository = dependencies.offerRepositoryComponent.offerRepositoryImpl
  private def eclairClient = dependencies.eclairClientComponent.eclairClient

  override def makeNewInvoice(session: Session, offerId: Long): Future[Long] = {
    logger.info(s"Fetching invoice for session: ${session.id}, offer:$offerId")

    lazy val existing = latestActiveInvoiceBy(session.id, offerId).map(_.id)
    val existingResult = Await.result(existing.future, 5 seconds)

    if (existingResult.isDefined)
      return Future.successful(existingResult.get)

    for {
      offer <- offerRepository.byId(offerId) orFailWith s"Offer $offerId not found"
      invoiceMsg = s"Please pay ${offer.price} satoshis for ${offer.description}, MAC:${session.clientMac}"
      eclairResponse <- eclairClient.getInvoice(offer.price, invoiceMsg)
      invoice = Invoice(paid = false, lnInvoice = eclairResponse, sessionId = Some(session.id), offerId = Some(offerId))
      invoiceId <- invoiceRepository.insert(invoice)
    } yield {
      logger.info(s"New invoice id=$invoiceId")
      invoiceId
    }

  }

  private def latestActiveInvoiceBy(sessionId: Long, offerId: Long): FutureOption[Invoice] = {
    invoiceRepository
      .activeInvoicesBySessionId(sessionId)
      .map { invoices =>
        invoices
          .filter(i => i.offerId == Some(offerId)) //TODO add expiration
          .sortWith((a, b) => a.createdAt.isAfter(b.createdAt))
          .headOption
      }
  }

  override def invoiceById(invoiceId: Long) = {
    for {
      invoice <- invoiceRepository.invoiceById(invoiceId)
      offer <- offerRepository.byId(invoice.offerId.get) //fixme
    } yield invoiceToDto(invoice, offer)
  }

  override def allOffers = offerRepository.allOffers

  override def offerById(id: Long) = offerRepository.byId(id)
}
