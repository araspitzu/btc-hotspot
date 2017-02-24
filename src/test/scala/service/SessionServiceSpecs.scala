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
import mocks.{IpTablesServiceMock, MockStopWatch}
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.specification.Scope
import protocol.SessionRepositoryImpl
import protocol.domain.{Offer, QtyUnit, Session}
import services.{OfferService, OfferServiceInterface, OfferServiceRegistry, SessionService}
import util.CleanRepository.CleanSessionRepository
import util.Helpers._
import watchdog.{StopWatch, TimebasedStopWatch}

class SessionServiceSpecs extends Specification with CleanSessionRepository with Mockito {
  sequential
  
  trait MockSessionServiceScope extends Scope {
    val offer = OfferServiceRegistry.offerService.allOffers.futureValue.head
    
    val sessionRepository: SessionRepositoryImpl = new SessionRepositoryImpl
    val offerService:OfferServiceInterface = new OfferService
    val ipTableService: IpTablesInterface = new {} with IpTablesServiceMock { }
  }
  
  "SessionService" should {
  
    val macAddress = "ab:12:cd:34:ef:56"
    
    "save and load session to db" in new MockSessionServiceScope {
      val sessionService = new SessionService(this)
      
      val sessionId = sessionService.getOrCreate(macAddress).futureValue
      val Some(session) = sessionService.byId(sessionId).futureValue

      session.id === sessionId
      session.clientMac === macAddress
    }
    
    "select the correct stopwatch for an offer" in new MockSessionServiceScope {
      val sessionService = new SessionService(this)
      
      val session = Session(clientMac = macAddress)
      
      val timeBasedOffer = Offer(
        qty = 25,
        qtyUnit = QtyUnit.millis,
        price = 1234,
        description = "Some offer"
      )
      
      val timeBasedStopwatch = sessionService.selectStopwatchForOffer(session.id, timeBasedOffer)
      
      timeBasedStopwatch must haveClass[TimebasedStopWatch]
      
    }
    
    "enable session should bind the session with the offer,start the stopwatch and enable traffic via iptables" in new MockSessionServiceScope {
      
      var ipTablesCalled = false
      //mock
      override val ipTableService = new IpTablesServiceMock {
        override def enableClient(mac: String): FutureOption[String] = {
          ipTablesCalled = true
          futureSome("")
        }
      }
  
      var stopWatchStarted = false
      
      val newSession = Session(clientMac = macAddress)
      
      val sessionService = new SessionService(this){
        override def selectStopwatchForOffer(sessionId: Long, offer: Offer):StopWatch = new MockStopWatch(sessionId, offer.offerId){
          override def start(onLimitReach: => Unit): Unit = {
            stopWatchStarted = true
            ()
          }
        }
      }
      
      newSession.offerId must beNone
      newSession.remainingUnits must beLessThan(0L)
  
      sessionService.enableSessionFor(newSession, offer.offerId).futureValue

      val Some(enabledSession) = sessionService.byMac(macAddress).futureValue
      sessionService.sessionIdToStopwatch.get(enabledSession.id) must beSome
      
      stopWatchStarted must beTrue
      ipTablesCalled must beTrue
      enabledSession.offerId === Some(offer.offerId)
      enabledSession.remainingUnits === offer.qty
      
    }
    
    "disable session should stop the stopwatch and disable iptable traffic" in new MockSessionServiceScope {
      var ipTablesCalled = false
      //mock
      override val ipTableService = new IpTablesServiceMock {
        override def enableClient(mac: String): FutureOption[String] = futureSome("")
        override def disableClient(mac: String): FutureOption[String] = {
          ipTablesCalled = true
          futureSome("")
        }
      }
  
  
      val sessionService = new SessionService(this)
  
      val newSessionId = sessionService.getOrCreate(macAddress).futureValue
      
      val Some(newSession) = sessionRepository.bySessionId(newSessionId).futureValue
      
      sessionService.enableSessionFor(newSession, offer.offerId).futureValue
      val Some(session) = sessionRepository.byMacAddress(newSession.clientMac).futureValue
      sessionService.sessionIdToStopwatch.get(session.id) must beSome
            
      sessionService.disableSession(newSession).futureValue
      
      ipTablesCalled must beTrue
      sessionService.sessionIdToStopwatch.get(newSession.id) must beNone
  
    }
    
  }
  
}
