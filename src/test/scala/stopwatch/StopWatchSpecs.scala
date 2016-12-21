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

package stopwatch
import commons.TestData
import iptables.IpTablesServiceImpl
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import protocol.domain.{Offer, Session}
import watchdog.{StopWatch, TimebasedStopWatch}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  * Created by andrea on 09/12/16.
  */
class StopWatchSpecs extends Specification with Mockito {
  
  trait MockStopWatch extends StopWatch {
    override val ipTablesService = mock[IpTablesServiceImpl]
//    override val sessionRepository = mock[SessionRepository.type]
//    override val scheduler = mock[Scheduler.type]
  }
  
  "TimeBasedS stop watch" should {

    "wait the correct time before calling onLimitReach" in {
      
      val session = Session(clientMac = "thisIsMyMac")
      val offer = TestData.offers.head
      
      val timeStopWatch = new TimebasedStopWatch(session, offer) with MockStopWatch {
        ipTablesService.enableClient(anyString) returns Future.successful("Yeah")
        //scheduler.schedule(session.id, any[FiniteDuration])
        //sessionRepository.upsert(any[Session]) returns Future.successful(None)
      }

      //timeStopWatch.start
      //there was one(timeStopWatch.sessionRepository).upsert(session)
      
      success
      
    }

  }
  
}
