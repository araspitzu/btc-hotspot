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

import fr.acinq.bitcoin.{ MilliSatoshi, Satoshi }
import protocol.domain.QtyUnit.QtyUnit

package object domain {

  case class Invoice(
    id: Long = -1,
    createdAt: LocalDateTime = LocalDateTime.now,
    expiresAt: LocalDateTime = LocalDateTime.now.plusMinutes(15),
    paid: Boolean = false,
    lnInvoice: String,
    sessionId: Option[Long],
    offerId: Option[Long]
  ) {

    def isExpired() = {
      val now = LocalDateTime.now
      now.isAfter(createdAt) && now.isBefore(expiresAt)
    }

  }

  case class Session(
    id: Long = -1,
    createdAt: LocalDateTime = LocalDateTime.now,
    clientMac: String,
    remainingUnits: Long = -1,
    offerId: Option[Long] = None
  )

  case class Offer(
    offerId: Long = -1,
    qty: Long,
    qtyUnit: QtyUnit,
    price: Long,
    description: String
  )

  case object QtyUnit extends Enumeration {
    type QtyUnit = Value

    val MB = Value("MB")
    val millis = Value("millis")
  }

  case class BitcoinTransaction(
    hash: String,
    value: Long,
    creationDate: Option[Date] = None
  )

  case class LightningInvoice(
    hash: String,
    value: Satoshi,
    date: LocalDateTime,
    testnet: Boolean
  )

}
