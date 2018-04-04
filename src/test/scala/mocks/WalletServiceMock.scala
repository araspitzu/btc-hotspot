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

package mocks

import commons.Helpers.FutureOption
import protocol.domain
import protocol.domain.{Invoice, Session}
import wallet.WalletServiceInterface

import scala.concurrent.Future

class WalletServiceMock extends WalletServiceInterface {
  override def generateInvoice(session: Session, offerId: Long): Future[Invoice] = ???

  override def getBalance(): Long = ???

  override def allTransactions(): Seq[domain.BitcoinTransaction] = ???

  override def spendTo(address: String, value: Long): Future[String] = ???

  override def checkInvoicePaid(invoiceId: Long): FutureOption[Boolean] = ???
}
