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

import commons.Helpers.FutureOption
import commons.TestData
import iptables.{IpTablesServiceComponent, IpTablesServiceImpl}
import mocks.IpTablesServiceMock
import registry.{SchedulerRegistry, SessionRepositoryRegistry}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import protocol.SessionRepositoryImpl
import protocol.domain.{Offer, QtyUnit, Session}
import watchdog.{SchedulerImpl, StopWatch, TimebasedStopWatch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class StopWatchSpecs extends Specification {

  trait MockEnv extends Scope {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val scheduler: SchedulerImpl = SchedulerRegistry.schedulerImpl
    val ipTableFun = new {} with IpTablesServiceMock {
      override def enableClient: (String) => Future[String] = { mac =>
        Future.successful("DONE")
      }
  
      override def disableClient: (String) => Future[String] = { mac =>
        Future.successful("Done again")
      }
    }
  }
  
  def waitForOfferMillis(offer: Offer) = {
    val nop = Future {
      Thread.sleep(offer.qty)
  
    }
    Await.result(nop, offer.qty + 100L millis )
  }
  

  
  "TimeBased stop watch" should {

    "wait the correct time before calling onLimitReach" in new MockEnv {
      
      val session = Session(clientMac = "thisIsMyMac")
      val offer = Offer(
        qty = 5000,
        qtyUnit = QtyUnit.millis,
        price = 950000,
        description =  "1 second"
      )
      

      val timeStopWatch = new TimebasedStopWatch(this, session, offer)
      
      timeStopWatch.start

      waitForOfferMillis(offer)

      timeStopWatch.remainingUnits === 0
  
    }

  }
  
}
