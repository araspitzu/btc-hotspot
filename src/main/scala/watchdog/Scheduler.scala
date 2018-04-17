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

import akka.actor.{ ActorSystem, Cancellable }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait Scheduler {

  def schedule(sessionId: Long, delay: FiniteDuration)(task: => Unit): Unit

  def isScheduled(sessionId: Long): Boolean

  def scheduledAt(sessionId: Long): Option[LocalDateTime]

  def remove(sessionId: Long): Unit

  def cancel(sessionId: Long): Boolean

}

class SchedulerImpl(implicit system: ActorSystem) extends Scheduler with LazyLogging {

  import system.dispatcher

  private case class Schedule(createdAt: LocalDateTime, cancellable: Cancellable)

  private val sessIdToScheduleMap = new scala.collection.mutable.HashMap[Long, Schedule]

  def schedule(sessionId: Long, delay: FiniteDuration)(task: => Unit): Unit = {

    val cancellable = system.scheduler.scheduleOnce(delay)(task)

    sessIdToScheduleMap += sessionId -> Schedule(LocalDateTime.now, cancellable)
  }

  def isScheduled(sessionId: Long): Boolean = sessIdToScheduleMap.get(sessionId).isDefined

  def scheduledAt(sessionId: Long): Option[LocalDateTime] = {
    sessIdToScheduleMap.get(sessionId).map(_.createdAt)
  }

  def remove(sessionId: Long): Unit = sessIdToScheduleMap.remove(sessionId)

  def cancel(sessionId: Long): Boolean = {
    sessIdToScheduleMap.get(sessionId) match {
      case None           => false
      case Some(schedule) => schedule.cancellable.cancel
    }
  }

}

