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

import org.specs2.mutable.{BeforeAfter, Specification}
import org.specs2.specification.Scope
import protocol.DatabaseComponent
import sarvices.{OfferService, SessionService}
import util.Helpers._


/**
  * Created by andrea on 09/12/16.
  */
class SessionServceSpecs extends Specification  {
  
 // def before = Repository.setupDb.futureValue
 // def after = Repository.db.shutdown
  
  
  "SessionService" should {
    
    "save and load session to db" in {
      val mac = "123"
      
      val sessionId = SessionService.getOrCreate(mac).futureValue
      val Some(session) = SessionService.byMac(mac).future.futureValue
      
      session.id === sessionId
      session.clientMac === mac
      
    }
    
  }
  
}
