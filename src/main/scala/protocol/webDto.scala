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

import protocol.domain.{ BitcoinTransaction, Offer }
import commons.Configuration.MiniPortalConfig._

package object webDto {

  case class WebOfferDto(
    offer: Offer,
    paymentURI: String
  )

  object WebOfferDto {
    def apply(offer: Offer): WebOfferDto = WebOfferDto(
      offer,
      paymentURI = s"bitcoin:?r=http://$miniPortalHost:$miniPortalPort/api/pay/${offer.offerId}"
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
