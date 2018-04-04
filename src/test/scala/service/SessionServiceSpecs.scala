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

import commons.Helpers.FutureOption
import iptables.IpTablesInterface
import mocks.{ IpTablesServiceMock, MockStopWatch, WalletServiceMock }
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.specification.Scope
import protocol.SessionRepositoryImpl
import protocol.domain.{ Offer, QtyUnit, Session }
import services.{ OfferService, OfferServiceInterface, OfferServiceRegistry, SessionServiceImpl }
import util.CleanRepository.CleanSessionRepository
import util.Helpers._
import wallet.WalletServiceInterface
import watchdog.{ StopWatch, TimebasedStopWatch }

class SessionServiceSpecs extends Specification with CleanSessionRepository with Mockito {
  sequential

  trait MockSessionServiceScope extends Scope {
    val bip70Payment = 222

    val stopWatchDepencencies = new {
      val ipTablesService: IpTablesInterface = new IpTablesServiceMock {}
    }

    val offer = OfferServiceRegistry.offerService.allOffers.futureValue.head

    val sessionRepository: SessionRepositoryImpl = new SessionRepositoryImpl
    val offerService: OfferServiceInterface = new OfferService
    val walletService: WalletServiceInterface = new WalletServiceMock {

    }
  }

  "SessionService" should {

    val macAddress = "ab:12:cd:34:ef:56"

    "save and load session to db" in new MockSessionServiceScope {
      val sessionService = new SessionServiceImpl(this)

      val sessionId = sessionService.getOrCreate(macAddress).futureValue
      val Some(session) = sessionService.byId(sessionId).futureValue

      session.id === sessionId
      session.clientMac === macAddress
    }

    "select the correct stopwatch for an offer" in new MockSessionServiceScope {
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

    "enable session" in new MockSessionServiceScope {

      var stopWatchStarted = false

      val newSession = Session(clientMac = macAddress)

      val sessionService = new SessionServiceImpl(this) {
        override def selectStopwatchForOffer(session: Session, offer: Offer): StopWatch = new MockStopWatch(stopWatchDepencencies, session, offer.offerId) {
          override def start(onLimitReach: => Unit): FutureOption[Unit] = {
            stopWatchStarted = true
            futureSome(Unit)
          }
        }
      }

      newSession.offerId must beNone
      newSession.remainingUnits must beLessThan(0L)

      sessionService.payAndEnableSessionForOffer(newSession, offer.offerId, bip70Payment).futureValue
      sessionService.sessionIdToStopwatch.get(newSession.id) must beSome

      val Some(enabledSession) = sessionService.byMac(macAddress).futureValue

      stopWatchStarted must beTrue
      enabledSession.offerId === Some(offer.offerId)
    }

    "disable session" in new MockSessionServiceScope {

      var stopWatchStarted = false
      var stopWatchStopped = false

      val sessionService = new SessionServiceImpl(this) {
        override def selectStopwatchForOffer(session: Session, offer: Offer): StopWatch = new MockStopWatch(stopWatchDepencencies, session, offer.offerId) {
          override def start(onLimitReach: => Unit) = {
            stopWatchStarted = true
            futureSome(Unit)
          }
          override def stop() = {
            stopWatchStopped = true
            futureSome(Unit)
          }
        }
      }

      val newSessionId = sessionService.getOrCreate(macAddress).futureValue

      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue

      stopWatchStarted must beFalse
      stopWatchStopped must beFalse
      sessionService.payAndEnableSessionForOffer(newSession, offer.offerId, 666).futureValue
      stopWatchStarted must beTrue
      stopWatchStopped must beFalse

      sessionService.disableSession(newSession).futureValue
      stopWatchStopped must beTrue
    }

  }

}
