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

package mocks

import protocol.domain.{Offer, Session}
import watchdog.StopWatch

import scala.concurrent.Future

class MockStopWatch(aSession: Long, aDuration: Long) extends StopWatch {
  override val sessionId: Long= aSession
  override val duration: Long = aDuration
  
  override def start(onLimitReach: => Unit): Unit = ???
  
  override def stop(): Unit = ???
  
  override def remainingUnits(): Long = ???
  
  override def isPending(): Boolean = ???

}
