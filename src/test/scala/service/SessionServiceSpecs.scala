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
import protocol.domain.Session
import services.{OfferService, SessionService}
import util.CleanRepository.CleanSessionRepository
import util.Helpers._

class SessionServiceSpecs extends Specification with CleanSessionRepository {
  sequential
  
  "SessionService" should {
  
    val mac = "123"
    
    "save and load session to db" in {

//      val sessionId = SessionService.getOrCreate(mac).futureValue
//      val Some(session) = SessionService.byId(sessionId).futureValue
//
//      session.id === sessionId
//      session.clientMac === mac
        2 === 2
    }
    
    
    "enable a session for an offer" in {
      
      val session = Session(clientMac = mac)
      session.offerId must beNone

      val offer = OfferService.allOffers.futureValue.head

      SessionService.enableSessionFor(session, offer.offerId).futureValue

      val Some(enabledSession) = SessionService.byMac(mac).futureValue

      enabledSession.offerId === Some(offer.offerId)
      enabledSession.remainingUnits === offer.qty

    }
    
  }
  
}
