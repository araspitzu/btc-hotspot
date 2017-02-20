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
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.concurrent.duration._


trait StopWatch extends LazyLogging {
  
  val sessionId:Long
  val duration: Long
  
  def start():Unit
  
  def stop():Unit
  
  def remainingUnits():Long
  
  def isActive():Boolean

  def onLimitReach():Unit
  
}

class TimebasedStopWatch(dependencies:{
   val scheduler: SchedulerImpl
}, val sessionId: Long, val duration: Long) extends StopWatch {
  
  import dependencies._
  
  
  override def start(): Unit = {
    scheduler.schedule(sessionId, duration millisecond) {                      //start countdown
      this.onLimitReach()
    }
    
  }
  
  override def stop(): Unit = {
    logger.info(s"Stopping ${sessionId}")
    // abort scheduled task
    if (isActive) scheduler.cancel(sessionId)
    
    
  }
  
  override def remainingUnits(): Long = {
    scheduler.scheduledAt(sessionId) match {
      case Some(scheduledAt) => ChronoUnit.MILLIS.between(LocalDateTime.now, scheduledAt)
      case None => throw new IllegalArgumentException(s"Could not find schedule for $sessionId")
    }
  }
  
  override def onLimitReach(): Unit = {
    logger.info(s"Reached offer limit for session $sessionId")
    this.stop()
  }
  
  override def isActive() = scheduler.isScheduled(sessionId)
  
}
//class DatabasedStopWatch extends StopWatch