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
import ln.EclairClient
import protocol.webDto._

import scala.concurrent.{ Await, ExecutionContext, Future, Promise }
import protocol.{ InvoiceRepositoryImpl, OfferRepositoryImpl, domain }

trait WalletService {

  def checkInvoicePaid(invoiceId: Long): Future[InvoicePaid]

  def getBalance(): Long //satoshis

  def allTransactions(): Seq[BitcoinTransaction]

  def spendTo(address: String, value: Long): Future[String]

}

class LightningServiceImpl(dependencies: {
  val eclairClient: EclairClient
  val invoiceRepository: InvoiceRepositoryImpl
  val offerRepository: OfferRepositoryImpl
})(implicit ec: ExecutionContext) extends WalletService with LazyLogging {

  import dependencies._

  override def checkInvoicePaid(invoiceId: Long): Future[InvoicePaid] = {
    for {
      invoice <- invoiceRepository.invoiceById(invoiceId) orFailWith s"Invoice $invoiceId not found"
      isPaid <- eclairClient.checkInvoice(invoice.lnInvoice)
      _ = if (invoice.paid != isPaid) {
        invoiceRepository.upsert(invoice.copy(paid = isPaid))
        logger.info(s"Invoice $invoiceId is now paid")
      }
    } yield InvoicePaid(invoiceId, isPaid)
  }

  override def getBalance(): Long = 666

  override def allTransactions(): Seq[domain.BitcoinTransaction] = Seq.empty

  override def spendTo(lnInvoice: String, value: Long): Future[String] = {
    logger.info(s"Sending $value to $lnInvoice")
    eclairClient.payInvoice(lnInvoice).map(_.paymentHash)
  }

}
