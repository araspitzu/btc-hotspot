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

package watchdog

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.typesafe.scalalogging.LazyLogging
import commons.Helpers.FutureOption
import iptables.IpTables
import protocol.domain.Session

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait StopWatch extends LazyLogging {

  type StopWatchDependency = {
    val ipTablesService: IpTables
  }

  val dependencies: StopWatchDependency
  val session: Session
  val duration: Long

  def start(onLimitReach: => Unit): Future[Unit]

  def stop(): Future[Unit]

  def remainingUnits(): Long

  def isPending(): Boolean

}

class TimebasedStopWatch(val dependencies: {
  val ipTablesService: IpTables
  val scheduler: Scheduler
}, val session: Session, val duration: Long)(implicit ec: ExecutionContext) extends StopWatch {
  import dependencies._

  override def start(onLimitReach: => Unit): Future[Unit] = {
    ipTablesService.enableClient(session.clientMac) map { ipTablesOut =>
      scheduler.schedule(session.id, duration millisecond) {
        logger.info(s"Reached limit for session ${session.id}")
        scheduler.remove(session.id)
        onLimitReach
      }
    }
  }

  override def stop(): Future[Unit] = {
    logger.info(s"stopwatch for session ${session.id} is stopping")
    ipTablesService.disableClient(session.clientMac) map { ipTablesOut =>
      // abort scheduled task
      if (isPending) {
        logger.info(s"Aborting scheduled task for session ${session.id}")
        scheduler.cancel(session.id)
      }
    }
  }

  override def remainingUnits(): Long = {
    scheduler.scheduledAt(session.id) match {
      case Some(scheduledAt) => ChronoUnit.MILLIS.between(LocalDateTime.now, scheduledAt)
      case None              => throw new IllegalArgumentException(s"Could not find schedule for ${session.id}")
    }
  }

  override def isPending() = scheduler.isScheduled(session.id)

}
//class DatabasedStopWatch extends StopWatch