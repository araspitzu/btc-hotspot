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

package protocol

import java.time.LocalDateTime
import java.util.Date

import protocol.domain._
import commons.Configuration.MiniPortalConfig._

package object webDto {

  def invoiceToDto(invoice: Invoice, offer: Offer): InvoiceDto = {
    InvoiceDto(
      invoice.id,
      invoice.createdAt,
      OfferDto(
        offer.offerId,
        offer.qty,
        offer.qtyUnit.toString,
        offer.price,
        offer.description
      ),
      invoice.lnInvoice,
      invoice.paid
    )
  }

  case class InvoicePaid(
    invoiceId: Long,
    paymentReceived: Boolean
  )

  case class InvoiceDto(
    id: Long,
    createdAt: LocalDateTime,
    offer: OfferDto,
    lnInvoice: String,
    paid: Boolean
  )

  case class OfferDto(
    offerId: Long = -1,
    qty: Long,
    qtyUnit: String,
    price: Long,
    description: String
  )

  case class OfferWebDto(
    offer: Offer,
    paymentURI: String
  )

  object OfferWebDto {
    def apply(offer: Offer): OfferWebDto = OfferWebDto(
      offer,
      paymentURI = s"http://$miniPortalHost:$miniPortalPort/invoice.html?offerId=${offer.offerId}"
    )
  }

  case class BitcoinTransactionDto(
    hash: String,
    value: Long,
    creationDate: Option[Date] = None,
    explorerUrl: String
  )

  object BitcoinTransactionDto {
    def apply(hash: String, value: Long, creationDate: Option[Date]): BitcoinTransactionDto = BitcoinTransactionDto(
      hash,
      value,
      creationDate = creationDate,
      explorerUrl = s"https://testnet.blockexplorer.com/tx/$hash"
    )

    def apply(btx: BitcoinTransaction): BitcoinTransactionDto = apply(btx.hash, btx.value, btx.creationDate)

  }

  case class WithdrawTransactionData(
    address: String,
    amount: Long
  )

}
