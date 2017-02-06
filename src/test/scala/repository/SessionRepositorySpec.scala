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

package repository

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.specs2.mutable.Specification
import protocol.SessionRepositoryImpl
import protocol.domain.Session
import util.CleanRepository.CleanSessionRepository
import util.Helpers._

class SessionRepositorySpec extends Specification with CleanSessionRepository with LazyLogging {
  sequential
  
  "Session repository" should {
    
    "save and retrieve a session by ID" in {
  
      val sessionRepositoryImpl = new SessionRepositoryImpl
      sessionRepositoryImpl.allSession.futureValue.length === 0
  
      val session = Session(clientMac = "someMac")
      session.id === -1
      
      val sessionId = sessionRepositoryImpl.insert(session).futureValue
  
      sessionId !== -1
      
      val Some(savedSession) = sessionRepositoryImpl.bySessionId(sessionId).futureValue
      savedSession.id === sessionId
      savedSession.clientMac === "someMac"
      
    }
    
    "update or insert a session (UPSERT)" in {
  
      val sessionRepositoryImpl = new SessionRepositoryImpl
      sessionRepositoryImpl.allSession.futureValue.length === 0
  
      val session = Session(clientMac = "someMacForTest")
      session.id === -1
      
      val Some(upsertedSessionId) = sessionRepositoryImpl.upsert(session).futureValue
      upsertedSessionId !== -1 //must have been set by DB
      
      val Some(savedSession) = sessionRepositoryImpl.bySessionId(upsertedSessionId).futureValue
      
      //Alter the session and save it again
      sessionRepositoryImpl.upsert(savedSession.copy(remainingUnits = 12345L)).futureValue
      
      val Some(updatedSession) = sessionRepositoryImpl.bySessionId(upsertedSessionId).futureValue
  
      updatedSession.clientMac === "someMacForTest"
      updatedSession.id === upsertedSessionId
      updatedSession.remainingUnits === 12345L
    }
    
  }
  
}
