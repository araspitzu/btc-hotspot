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
import akka.actor.Cancellable
import scala.concurrent.duration.FiniteDuration
import commons.AppExecutionContextRegistry.context._
/**
  * Created by andrea on 05/12/16.
  */
object Scheduler {
  
  private case class Schedule(createdAt:LocalDateTime, cancellable: Cancellable)
  
  private val tasks = new scala.collection.mutable.HashMap[Long, Schedule]
  
  def schedule(sessionId:Long, delay: FiniteDuration)(task: Unit):Unit = {
    
    val cancellable = actorSystem.scheduler.scheduleOnce(delay)(task)
    val schedule = Schedule(LocalDateTime.now, cancellable)
    
    tasks += sessionId -> schedule
  }
  
  def scheduledAt(sessionId:Long):Option[LocalDateTime] = {
    tasks.get(sessionId).map(_.createdAt)
  }

  def cancel(sessionId:Long):Boolean = {
    tasks.get(sessionId) match {
      case None => false
      case Some(schedule) => schedule.cancellable.cancel
    }
  }
   
}