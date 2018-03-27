package wallet

import com.typesafe.scalalogging.LazyLogging
import commons.Helpers
import ln.EclairClient
import org.bitcoin.protocols.payments.Protos
import protocol.domain
import services.OfferServiceRegistry
import commons.AppExecutionContextRegistry.context._
import scala.concurrent.Future

class LightningServiceImpl extends WalletServiceInterface with LazyLogging {

  val eclairClient: EclairClient = ???

  override def generateInvoice(session: domain.Session, offerId: Long): Future[String] = {
    logger.info(s"Issuing payment request for session ${session.id} and offer $offerId")
    (for {
      offer <- OfferServiceRegistry.offerService.offerById(offerId)
      eclairResponse <- eclairClient.getInvoice(offer.price, s"Please pay ${offer.price} satoshis for ${offer.description}").map(Some(_))
    } yield eclairResponse
    ).future.map(_.getOrElse(throw new IllegalArgumentException(s"Offer $offerId not found")))
  }

  override def validateBIP70Payment(payment: Protos.Payment): Helpers.FutureOption[Protos.PaymentACK] = ???

  override def getBalance(): Long = 666

  override def allTransactions(): Seq[domain.BitcoinTransaction] = Seq.empty

  override def spendTo(lnInvoice: String, value: Long): Future[String] = {
    logger.info(s"Sending $value to $lnInvoice")
    eclairClient.sendTo(lnInvoice, value)
  }

}
