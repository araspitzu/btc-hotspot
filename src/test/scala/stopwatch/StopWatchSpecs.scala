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

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.typesafe.scalalogging.LazyLogging
import commons.Helpers.FutureOption
import iptables.IpTables
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import protocol.domain.{ Offer, QtyUnit, Session }
import util.Helpers._
import watchdog.{ SchedulerImpl, StopWatch, TimebasedStopWatch }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class StopWatchSpecs extends Specification with LazyLogging with Mockito {
  sequential

  implicit val system = ActorSystem("test-actor-system")

  trait MockStopWatchScope extends Scope {
    val offer = Offer(
      qty = 2000,
      qtyUnit = QtyUnit.millis,
      price = 950000,
      description = "1 second"
    )

    val session = Session(clientMac = "thisIsMyMac")

    val stopWatchDep = new {

      val ipTablesService: IpTables = mock[IpTables]

      ipTablesService.enableClient(session.clientMac) returns Future.successful("")
      ipTablesService.disableClient(session.clientMac) returns Future.successful("")

      val scheduler: SchedulerImpl = new SchedulerImpl
    }
  }

  def waitForMillis(waitDuration: Long) = {
    val nop = Future {
      Thread.sleep(waitDuration + 500L)
    }
    Await.result(nop, waitDuration + 1000L millis)
  }

  "TimeBased stop watch" should {

    "wait the correct time before calling onLimitReach" in new MockStopWatchScope {

      val approximation = 500L //TODO review 0.5s approximation during test

      val timeStopWatch = new TimebasedStopWatch(stopWatchDep, session, offer.qty)

      var t2 = -1L
      val t1 = System.currentTimeMillis
      var onReachCalled = false

      timeStopWatch.start(onLimitReach = {
        logger.info("calling onLimitReach")
        t2 = System.currentTimeMillis
        onReachCalled = true
      })

      waitForMillis(offer.qty)

      onReachCalled must beTrue
      t2 !== -1L
      t2 - t1 - approximation must beCloseTo(offer.qty within 1.significantFigures)
    }

    "isPending should return true if the stopwatch is still running, false otherwise" in new MockStopWatchScope {

      val timeStopWatch = new TimebasedStopWatch(stopWatchDep, session, offer.qty)

      timeStopWatch.start(onLimitReach = {
        logger.info("calling onLimitReach")
      })

      timeStopWatch.isPending must beTrue

      waitForMillis(offer.qty)

      timeStopWatch.isPending must beFalse
    }

  }

}
