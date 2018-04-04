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

import java.io.File
import java.time.{ LocalDate, LocalDateTime }
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import commons.Configuration.WalletConfig._
import commons.Configuration.MiniPortalConfig._
import protocol.domain._
import services.{ OfferServiceRegistry, SessionServiceRegistry }

import scala.collection.JavaConverters._
import commons.AppExecutionContextRegistry.context._
import commons.Helpers
import commons.Helpers.FutureOption
import ln.{ EclairClient, EclairClientImpl }

import scala.concurrent.{ Future, Promise }
import protocol.domain
import protocol.webDto.InvoiceDto
import registry.Registry

object WalletServiceRegistry extends Registry with WalletServiceComponent {

  override val walletService: WalletServiceInterface = new LightningServiceImpl

}

trait WalletServiceComponent {

  val walletService: WalletServiceInterface

}

trait WalletServiceInterface {

  def generateInvoice(session: Session, offerId: Long): Future[Invoice]

  def checkInvoicePaid(invoice: Invoice): Future[Boolean]

  def getBalance(): Long //satoshis

  def allTransactions(): Seq[BitcoinTransaction]

  def spendTo(address: String, value: Long): Future[String]

}

class LightningServiceImpl(dependencies: {
  val eclairClient: EclairClient
}) extends WalletServiceInterface with LazyLogging {

  private def eclairClient = dependencies.eclairClient

  def this() = this(new {
    val eclairClient = new EclairClientImpl
  })

  override def generateInvoice(session: domain.Session, offerId: Long): Future[Invoice] = {
    logger.info(s"Issuing payment request for session ${session.id} and offer $offerId")
    (for {
      offer <- OfferServiceRegistry.offerService.offerById(offerId)
      eclairResponse <- eclairClient.getInvoice(offer.price, s"Please pay ${offer.price} satoshis for ${offer.description}").map(Some(_))
    } yield eclairResponse
    ).future.map(???)
  }

  override def checkInvoicePaid(invoice: Invoice): Future[Boolean] = ???

  override def getBalance(): Long = 666

  override def allTransactions(): Seq[domain.BitcoinTransaction] = Seq.empty

  override def spendTo(lnInvoice: String, value: Long): Future[String] = {
    logger.info(s"Sending $value to $lnInvoice")
    eclairClient.sendTo(lnInvoice, value)
  }

}
