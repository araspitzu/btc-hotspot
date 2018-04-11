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

import iptables.IpTablesInterface
import mocks.{ DatabaseComponentMock, IpTablesServiceMock, MockStopWatch, WalletServiceMock }
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.specification.Scope
import protocol.{ InvoiceRepositoryImpl, OfferRepositoryImpl, SessionRepositoryImpl }
import protocol.domain.{ Invoice, Offer, QtyUnit, Session }
import services._
import util.CleanRepository.CleanSessionRepository
import util.Helpers._
import wallet.WalletServiceInterface
import watchdog.{ StopWatch, TimebasedStopWatch }

import scala.concurrent.Future

class SessionServiceSpecs extends Specification with CleanSessionRepository with Mockito {
  sequential

  trait MockSessionServiceScope extends Scope {

    val stopWatchDepencencies = new {
      val ipTablesService: IpTablesInterface = new IpTablesServiceMock {}
    }

    val offer = InvoiceServiceRegistry.invoiceService.allOffers.futureValue.head

    val sessionRepository: SessionRepositoryImpl = new SessionRepositoryImpl(DatabaseComponentMock)
    val invoiceRepository: InvoiceRepositoryImpl = new InvoiceRepositoryImpl
    val offerRepository: OfferRepositoryImpl = new OfferRepositoryImpl

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

      var stopWatchStarted = false

      val sessionService = new SessionServiceImpl(this) {
        override def selectStopwatchForOffer(session: Session, offer: Offer): StopWatch = new MockStopWatch(stopWatchDepencencies, session, offer.offerId) {
          override def start(onLimitReach: => Unit): Future[Unit] = {
            stopWatchStarted = true
            Future.successful()
          }
        }
      }

      val newSessionId = sessionService.getOrCreate(macAddress).futureValue
      val newInvoice = Invoice(
        paid = true,
        lnInvoice = "ln-invoice-here",
        sessionId = Some(newSessionId),
        offerId = Some(offer.offerId)
      )
      val newInvoiceId = invoiceRepository.insert(newInvoice).futureValue
      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue

      sessionService.enableSessionForInvoice(newSession, newInvoiceId).futureValue
      sessionService.sessionIdToStopwatch.get(newSessionId) must beSome

      val Some(enabledSession) = sessionService.byMac(macAddress).futureValue

      stopWatchStarted must beTrue
      enabledSession.offerId === Some(offer.offerId)
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
