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

import org.specs2.mutable._
import protocol.domain.{Offer, Session}
import services.{OfferService, OfferServiceRegistry, SessionService}
import util.CleanRepository.CleanSessionRepository
import util.Helpers._
import watchdog.StopWatch

import scala.concurrent.Future

class SessionServiceSpecs extends Specification with CleanSessionRepository {
  sequential
  
  class MockSessionService extends SessionService {
    var stopWatchStarted = false
    
    override def selectStopwatchForSession(aSession: Session, anOffer: Offer):StopWatch = {
      new {} with StopWatch {
        override def stop() = ???
        
        override def remainingUnits() = ???
        
        override def isActive() = ???
        
        override def onLimitReach() = ???
        
        override def start() = {
          stopWatchStarted = true
          Future.successful(None)
        }
        
        override val session: Session = aSession
        override val offer: Offer = anOffer
      }
    }
  }
  
  "SessionService" should {
  
    val mac = "123"
    
    "save and load session to db" in {
      val mockSessionService = new MockSessionService
      
      val sessionId = mockSessionService.getOrCreate(mac).futureValue
      val Some(session) = mockSessionService.byId(sessionId).futureValue

      session.id === sessionId
      session.clientMac === mac
    }
    
    
    "enable a session for an offer" in {
      
      val session = Session(clientMac = mac)
      session.offerId must beNone
      
      val mockSessionService = new MockSessionService
      
      val offer = OfferServiceRegistry.offerService.allOffers.futureValue.head
  
      mockSessionService.enableSessionFor(session, offer.offerId).futureValue

      val Some(enabledSession) = mockSessionService.byMac(mac).futureValue
      mockSessionService.stopWatchStarted must beTrue

      enabledSession.offerId === Some(offer.offerId)

    }
    
  }
  
}
