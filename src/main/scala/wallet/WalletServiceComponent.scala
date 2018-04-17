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
import services.SessionServiceRegistry
import commons.AppExecutionContextRegistry.context._
import commons.Helpers.FutureOption
import ln.{ EclairClient, EclairClientImpl, EclairClientRegistry }
import protocol.webDto._

import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.duration._
import protocol.{ InvoiceRepositoryImpl, OfferRepositoryImpl, domain }
import registry.{ OfferRepositoryRegistry, Registry }

object WalletServiceRegistry extends Registry with WalletServiceComponent {

  override val walletService: WalletServiceInterface = new LightningServiceImpl

}

trait WalletServiceComponent {

  val walletService: WalletServiceInterface

}

trait WalletServiceInterface {

  def checkInvoicePaid(invoiceId: Long): Future[InvoicePaid]

  def getBalance(): Long //satoshis

  def allTransactions(): Seq[BitcoinTransaction]

  def spendTo(address: String, value: Long): Future[String]

}

class LightningServiceImpl(dependencies: {
  val eclairClient: EclairClient
  val invoiceRepository: InvoiceRepositoryImpl
  val offerRepository: OfferRepositoryImpl
}) extends WalletServiceInterface with LazyLogging {

  private def eclairClient = dependencies.eclairClient
  private def invoiceRepository = dependencies.invoiceRepository
  private def offerRepository = dependencies.offerRepository

  def this() = this(new {
    val eclairClient = EclairClientRegistry.eclairClient
    val invoiceRepository = ???
    val offerRepository: OfferRepositoryImpl = OfferRepositoryRegistry.offerRepositoryImpl
  })

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
