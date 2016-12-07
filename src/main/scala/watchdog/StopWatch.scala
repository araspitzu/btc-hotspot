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

import com.typesafe.scalalogging.slf4j.LazyLogging
import protocol.domain.{Offer, Session}
import protocol.domain.QtyUnit._

/**
  * Created by andrea on 07/12/16.
  */
object StopWatch {
 
  def forOffer(session: Session, offer:Offer):StopWatch = offer.qtyUnit match {
    case MB => ???
    case minutes => TimebasedStopWatch(session, offer)
  }
  
}

sealed trait StopWatch extends LazyLogging {
  
  val session:Session
  val offer:Offer
  
  def start:Unit
  
  def stop:Unit
  
  def remainingTime:Long
  
  def onLimitReach:Unit
  
}

case class TimebasedStopWatch(session: Session, offer: Offer) extends StopWatch {
  
  override def start: Unit = {
    //alter iptables
    //start scheduler
    //update remaining time in session
  }
  
  override def stop: Unit = {
    //alter iptables
    //abort scheduled task
    //update remaining time in session
  }
  
  override def remainingTime: Long = {
    //check scheduler
    //update remaining time in session?
    ???
  }
  
  override def onLimitReach: Unit = {
    //stop session
  }
  
}
//class DatabasedStopWatch extends StopWatch