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
import protocol.domain.{ Invoice, Session }
import services.SessionServiceImpl
import util.Helpers._
import watchdog.{ Scheduler, SchedulerImpl }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceSpecs extends Specification with Mockito {
  sequential

  implicit val system = ActorSystem("test-actor-system")

  trait MockSessionServiceScope extends Scope {

    val offer = TestData.offers.head.copy(qty = 1000000)

    val sessionRepository: SessionRepositoryImpl = mock[SessionRepositoryImpl]
    val offerRepository: OfferRepositoryImpl = mock[OfferRepositoryImpl]
    val invoiceRepository: InvoiceRepositoryImpl = mock[InvoiceRepositoryImpl]
    val schedulerService: Scheduler = new SchedulerImpl
    val ipTablesService: IpTables = mock[IpTables]
  }

  "SessionService" should {

    val macAddress = "ab:12:cd:34:ef:56"

    //    "save and load session to db" in new MockSessionServiceScope {
    //      val sessionService = new SessionServiceImpl(this)
    //
    //      val sessionId = sessionService.getOrCreate(macAddress).futureValue
    //      val Some(session) = sessionService.byId(sessionId).futureValue
    //
    //      session.id === sessionId
    //      session.clientMac === macAddress
    //    }
    //
    //    "select the correct stopwatch for an offer" in new MockSessionServiceScope {
    //      val sessionService = new SessionServiceImpl(this)
    //
    //      val session = Session(clientMac = macAddress)
    //
    //      val timeBasedOffer = Offer(
    //        qty = 25,
    //        qtyUnit = QtyUnit.millis,
    //        price = 1234,
    //        description = "Some offer"
    //      )
    //
    //      val timeBasedStopwatch = sessionService.selectStopwatchForOffer(session, timeBasedOffer)
    //
    //      timeBasedStopwatch must haveClass[TimebasedStopWatch]
    //
    //    }

    "enable session" in new MockSessionServiceScope {

      val newInvoice = Invoice(paid = true, lnInvoice = "ln-invoice-here", sessionId = Some(1L), offerId = Some(offer.offerId))

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

      // Start
      val sessionService: SessionServiceImpl = new SessionServiceImpl(this)

      val newSessionId = 1L
      val newInvoiceId = invoiceRepository.insert(newInvoice).futureValue

      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue

      sessionService.enableSessionForInvoice(newSession, newInvoiceId).futureValue
      sessionService.sessionIdToStopwatch.get(newSessionId) must beSome

      val Some(enabledSession) = sessionService.byMac(macAddress).futureValue

      //enabledSession.offerId === Some(offer.offerId)
    }

    //    "disable session" in new MockSessionServiceScope {
    //
    //      var stopWatchStarted = false
    //      var stopWatchStopped = false
    //
    //      val sessionService = new SessionServiceImpl(this) {
    //        override def selectStopwatchForOffer(session: Session, offer: Offer): StopWatch = new MockStopWatch(stopWatchDepencencies, session, offer.offerId) {
    //          override def start(onLimitReach: => Unit) = {
    //            stopWatchStarted = true
    //            Future.successful()
    //          }
    //          override def stop() = {
    //            stopWatchStopped = true
    //            Future.successful()
    //          }
    //        }
    //      }
    //
    //      val newSessionId = sessionService.getOrCreate(macAddress).futureValue
    //
    //      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue
    //
    //      newSession.id === 1
    //      stopWatchStarted must beFalse
    //      stopWatchStopped must beFalse
    //      sessionService.enableSessionForInvoice(newSession, offer.offerId).futureValue
    //      stopWatchStarted must beTrue
    //      stopWatchStopped must beFalse
    //
    //      sessionService.disableSession(newSession).futureValue
    //      stopWatchStopped must beTrue
    //    }

  }

}
