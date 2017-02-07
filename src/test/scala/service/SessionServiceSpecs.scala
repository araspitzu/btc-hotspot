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
import mocks.MockStopWatch
import org.specs2.mutable._
import protocol.SessionRepositoryImpl
import protocol.domain.{Offer, QtyUnit, Session}
import registry.{IpTablesServiceRegistry, SchedulerRegistry, SessionRepositoryRegistry}
import services.{OfferService, OfferServiceInterface, OfferServiceRegistry, SessionService}
import util.CleanRepository.CleanSessionRepository
import util.Helpers._
import watchdog.{SchedulerImpl, StopWatch, TimebasedStopWatch}

import scala.concurrent.Future

class SessionServiceSpecs extends Specification with CleanSessionRepository {
  sequential
  
  class MockSessionService extends SessionService
  
  "SessionService" should {
  
    val macAddress = "123"
    
    "save and load session to db" in {
      val mockSessionService = new MockSessionService
      
      val sessionId = mockSessionService.getOrCreate(macAddress).futureValue
      val Some(session) = mockSessionService.byId(sessionId).futureValue

      session.id === sessionId
      session.clientMac === macAddress
    }
    
    "select the correct stopwatch for an offer" in {
      
      val mockSessionService = new MockSessionService {
        override def selectStopwatchForSession(session: Session, offer: Offer): StopWatch = super.selectStopwatchForSession(session, offer)
      }
      
      val session = Session(clientMac = macAddress)
      
      val timeBasedOffer = Offer(
        qty = 25,
        qtyUnit = QtyUnit.millis,
        price = 1234,
        description = "Some offer"
      )
      
      val timeBasedStopwatch = mockSessionService.selectStopwatchForSession(session, timeBasedOffer)
      
      timeBasedStopwatch must haveClass[TimebasedStopWatch]
      
    }
    
    "enable session should bind the session with the offer and start the stopwatch" in {
      val session = Session(clientMac = macAddress)
  
      val mockSessionService = new MockSessionService {
        var stopWatchStarted = false
  
        override def selectStopwatchForSession(session: Session, offer: Offer):StopWatch = {
          new MockStopWatch(session, offer) {
            override def start() = {
              stopWatchStarted = true
              Future.successful(None)
            }
          }
        }
      }
  
      val offer = OfferServiceRegistry.offerService.allOffers.futureValue.head
      
      session.offerId must beNone
  
      mockSessionService.enableSessionFor(session, offer.offerId).futureValue

      val Some(enabledSession) = mockSessionService.byMac(macAddress).futureValue
  
      enabledSession.offerId === Some(offer.offerId)
      mockSessionService.stopWatchStarted must beTrue

    }
    
    
    
  }
  
}
