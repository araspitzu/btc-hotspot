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
import watchdog.{SchedulerImpl, StopWatch, TimebasedStopWatch}

import scala.concurrent.Future

class SessionServiceSpecs extends Specification with CleanSessionRepository with Mockito {
  sequential
  
  trait MockSessionServiceScope extends Scope {
    val sessionRepository: SessionRepositoryImpl = new SessionRepositoryImpl
    val offerService:OfferServiceInterface = new OfferService
    val ipTableService: IpTablesInterface = new {} with IpTablesServiceMock { }
  }
  
  "SessionService" should {
  
    val macAddress = "123"
    
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
    
    "enable session should bind the session with the offer and start the stopwatch" in new MockSessionServiceScope {
      //mock
      override val ipTableService = new IpTablesServiceMock {
        override def enableClient(mac: String): FutureOption[String] = FutureOption(
          Future.successful(Some(""))
        )
      }
  
      var stopWatchStarted = false
      
      //mock
      val sessionService = new SessionService(this){
        override def selectStopwatchForOffer(sessionId: Long, offer: Offer):StopWatch = new MockStopWatch(sessionId, offer.offerId){
          override def start(onLimitReach:() => Unit): Unit = {
            stopWatchStarted = true
            ()
          }
        }
      }

      val session = Session(clientMac = macAddress)
  
      val offer = OfferServiceRegistry.offerService.allOffers.futureValue.head
      
      session.offerId must beNone
      session.remainingUnits must beLessThan(0L)
  
      sessionService.enableSessionFor(session, offer.offerId).futureValue

      val Some(enabledSession) = sessionService.byMac(macAddress).futureValue
  
      stopWatchStarted must beTrue
      enabledSession.offerId === Some(offer.offerId)
      enabledSession.remainingUnits === offer.qty
      
    }
    
    
    
  }
  
}
