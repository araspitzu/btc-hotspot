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

package service

import akka.actor.ActorSystem
import commons.TestData
import iptables.IpTables
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.specification.Scope
import protocol.{ InvoiceRepositoryImpl, OfferRepositoryImpl, SessionRepositoryImpl }
import protocol.domain.{ Invoice, Offer, QtyUnit, Session }
import services.SessionServiceImpl
import util.Helpers._
import watchdog.{ Scheduler, SchedulerImpl, TimebasedStopWatch }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceSpecs extends Specification with Mockito {
  sequential

  implicit val system = ActorSystem("test-actor-system")

  trait baseMockScope extends Scope {

    val offer = TestData.offers.head.copy(qty = 1000000)
    val macAddress = "ab:12:cd:34:ef:56"

    val sessionRepository: SessionRepositoryImpl = mock[SessionRepositoryImpl]
    val offerRepository: OfferRepositoryImpl = mock[OfferRepositoryImpl]
    val invoiceRepository: InvoiceRepositoryImpl = mock[InvoiceRepositoryImpl]
    val schedulerService: Scheduler = new SchedulerImpl
    val ipTablesService: IpTables = mock[IpTables]
  }

  trait fullBlownMockScope extends baseMockScope {
    val newInvoice = Invoice(id = 1, paid = true, lnInvoice = "ln-invoice-here", sessionId = Some(1L), offerId = Some(offer.offerId))

    val newInvoiceId = newInvoice.id
    val newSessionId = 1L

    ipTablesService.enableClient(macAddress) returns Future.successful("")

    sessionRepository.byMacAddress(macAddress) returns Future.successful(Some(
      Session(id = 1L, clientMac = macAddress)
    ))

    sessionRepository.insert(any[Session]) returns Future.successful(1L)
    sessionRepository.upsert(any[Session]) returns Future.successful(Some(1L))
    sessionRepository.bySessionId(1L) returns Future.successful(Some(
      Session(id = 1L, clientMac = macAddress)
    ))

    invoiceRepository.insert(any[Invoice]) returns Future.successful(1L)
    invoiceRepository.invoiceById(1l) returns Future.successful(Some(newInvoice))

    offerRepository.byId(anyLong) returns Future.successful(Some(offer))

  }

  "SessionService" should {

    "select the correct stopwatch for an offer" in new baseMockScope {
      val sessionService = new SessionServiceImpl(this)

      val session = Session(clientMac = macAddress)

      val timeBasedOffer = Offer(
        qty = 25,
        qtyUnit = QtyUnit.millis,
        price = 1234,
        description = "Some offer"
      )

      val timeBasedStopwatch = sessionService.selectStopwatchForOffer(session, timeBasedOffer)

      timeBasedStopwatch must haveClass[TimebasedStopWatch]

    }

    "enable session" in new fullBlownMockScope {

      // Start
      val sessionService = new SessionServiceImpl(this)

      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue

      sessionService.enableSessionForInvoice(newSession, newInvoiceId).futureValue
      sessionService.sessionIdToStopwatch.get(newSessionId) must beSome

      val Some(enabledSession) = sessionService.byMac(macAddress).futureValue

      //enabledSession.offerId === Some(offer.offerId)
    }

    "disable session" in new fullBlownMockScope {

      ipTablesService.disableClient(macAddress) returns Future.successful("")

      val sessionService = new SessionServiceImpl(this)

      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue

      sessionService.enableSessionForInvoice(newSession, newInvoiceId).futureValue
      there was one(ipTablesService).enableClient(macAddress)
      sessionService.sessionIdToStopwatch.get(newSessionId) must beSome

      sessionService.disableSession(newSession).futureValue
      there was one(ipTablesService).disableClient(macAddress)
      sessionService.sessionIdToStopwatch.get(newSessionId) must beNone

    }

  }

}
