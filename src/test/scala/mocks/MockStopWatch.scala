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

import iptables.IpTablesInterface
import protocol.domain.Session
import watchdog.StopWatch

import scala.concurrent.Future

class MockStopWatch(val dependencies: {
  val ipTablesService: IpTablesInterface
}, aSession: Session, aDuration: Long) extends StopWatch {
  override val session: Session = aSession
  override val duration: Long = aDuration

  override def start(onLimitReach: => Unit): Future[Unit] = ???

  override def stop(): Future[Unit] = ???

  override def remainingUnits(): Long = ???

  override def isPending(): Boolean = ???

}
