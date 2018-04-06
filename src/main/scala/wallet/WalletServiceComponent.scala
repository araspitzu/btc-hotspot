/*
 * btc-hotspot
 * Copyright (C) 2016  Andrea Raspitzu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wallet

import com.typesafe.scalalogging.LazyLogging
import protocol.domain._
import services.{ OfferServiceRegistry, SessionServiceRegistry }
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import ln.{ EclairClient, EclairClientImpl }
import protocol.webDto._
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.duration._
import protocol.{ InvoiceRepositoryImpl, domain }
import registry.{ InvoiceRepositoryRegistry, Registry }

object WalletServiceRegistry extends Registry with WalletServiceComponent {

  override val walletService: WalletServiceInterface = new LightningServiceImpl

}

trait WalletServiceComponent {

  val walletService: WalletServiceInterface

}

trait WalletServiceInterface {

  def generateInvoice(session: Session, offerId: Long): Future[InvoiceDto]

  def checkInvoicePaid(invoiceId: Long): Future[InvoicePaid]

  def getBalance(): Long //satoshis

  def allTransactions(): Seq[BitcoinTransaction]

  def spendTo(address: String, value: Long): Future[String]

}

class LightningServiceImpl(dependencies: {
  val eclairClient: EclairClient
  val invoiceRepository: InvoiceRepositoryImpl
}) extends WalletServiceInterface with LazyLogging {

  private def eclairClient = dependencies.eclairClient
  private def invoiceRepository = dependencies.invoiceRepository

  def this() = this(new {
    val eclairClient = new EclairClientImpl
    val invoiceRepository = InvoiceRepositoryRegistry.invoiceRepositoryImpl
  })

  def getFirstActiveInvoiceBySessionAndOffer(sessionId: Long, offerId: Long): FutureOption[Invoice] = {
    invoiceRepository
      .activeInvoicesBySessionId(sessionId)
      .map { invoices =>
        invoices
          .filter(i => i.offerId == Some(offerId)) //TODO add expiration
          .sortWith((a, b) => a.createdAt.isAfter(b.createdAt))
          .headOption
      }
  }

  override def generateInvoice(session: domain.Session, offerId: Long): Future[InvoiceDto] = {
    logger.info(s"Fetching invoice for session ${session.id} and offer $offerId")

    lazy val existing = for {
      offer <- OfferServiceRegistry.offerService.offerById(offerId)
      exInv <- getFirstActiveInvoiceBySessionAndOffer(session.id, offerId)
    } yield invoiceToDto(exInv, offer)

    val existingResult = Await.result(existing.future, 5 seconds)
    if (existingResult.isDefined)
      return Future.successful(existingResult.get)

    for {
      offer <- OfferServiceRegistry.offerService.offerById(offerId) orFailWith s"Offer $offerId not found"
      invoiceMsg = s"Please pay ${offer.price} satoshis for ${offer.description}, MAC:${session.clientMac}"
      eclairResponse <- eclairClient.getInvoice(offer.price, invoiceMsg)
      invoice = Invoice(paid = false, lnInvoice = eclairResponse, sessionId = Some(session.id), offerId = Some(offerId))
      invoiceId <- invoiceRepository.insert(invoice)
    } yield invoiceToDto(invoice.copy(id = invoiceId), offer)

  }

  override def checkInvoicePaid(invoiceId: Long): Future[InvoicePaid] = {
    for {
      invoice <- invoiceRepository.invoiceById(invoiceId) orFailWith s"Invoice $invoiceId not found"
      isPaid <- eclairClient.checkInvoice(invoice.lnInvoice)
      //   updated <- invoiceRepository.upsert(invoice.copy(paid = isPaid)) orFailWith s"Error updating $invoice in DB"
    } yield InvoicePaid(invoiceId, isPaid)
  }

  override def getBalance(): Long = 666

  override def allTransactions(): Seq[domain.BitcoinTransaction] = Seq.empty

  override def spendTo(lnInvoice: String, value: Long): Future[String] = {
    logger.info(s"Sending $value to $lnInvoice")
    eclairClient.payInvoice(lnInvoice, value)
  }

}
