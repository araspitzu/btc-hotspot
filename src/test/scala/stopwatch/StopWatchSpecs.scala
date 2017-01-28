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
//import org.mockito.Mockito
import org.specs2.mock.{Mockito => Specs2Mockito}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import protocol.SessionRepositoryImpl
import protocol.domain.{Offer, QtyUnit, Session}
import watchdog.{SchedulerImpl, StopWatch, TimebasedStopWatch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


trait MockIptablesComponent extends IpTablesServiceComponent {
  import org.mockito.Mockito._
  override val ipTablesServiceImpl = mock[IpTablesServiceImpl](classOf[IpTablesServiceImpl])
}

class StopWatchSpecs extends Specification with Specs2Mockito {
  
  
  def waitForOfferMillis(offer: Offer) = {
    val nop = Future {
      Thread.sleep(offer.qty)
  
    }
    Await.result(nop, offer.qty + 100L millis )
  }
  

  
  "TimeBased stop watch" should {

    "wait the correct time before calling onLimitReach" in {
      
      val session = Session(clientMac = "thisIsMyMac")
      val offer = Offer(
        qty = 5000,
        qtyUnit = QtyUnit.millis,
        price = 950000,
        description =  "1 second"
      )
  

      val timeStopWatch = new TimebasedStopWatch(session, offer) with MockIptablesComponent
      
      spy(timeStopWatch.ipTablesServiceImpl).enableClient(anyString) returns Future.successful("Yo")

      timeStopWatch.start

      waitForOfferMillis(offer)

      timeStopWatch.remainingUnits === 0
  
    }

  }
  
}
