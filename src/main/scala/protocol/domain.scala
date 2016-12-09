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

package protocol

import java.time.LocalDateTime

import com.typesafe.scalalogging.slf4j.LazyLogging
import protocol.domain.QtyUnit.QtyUnit
import sarvices.OfferService
import watchdog.StopWatch

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
/**
  * Created by andrea on 15/11/16.
  */
package object domain extends LazyLogging {

  case class Session(
    id:Long = -1,
    createdAt:LocalDateTime = LocalDateTime.now,
    clientMac:String,
    remainingUnits:Long = -1,
    offerId: Option[Long] = None
  ) {
    
    
    private def stopWatchOrFail:StopWatch = stopwatch match {
      case None => throw new IllegalAccessException(s"Unable to use session without a stopwatch, offerId is $offerId")
      case Some(stopWatch) => stopWatch
    }
    
    private lazy val stopwatch:Option[StopWatch] = ???
//      offerId flatMap { id =>
//      Await.result(OfferService.offerById(id), 5 seconds) map {
//        StopWatch.forOffer(this, _)
//      }
//    }
    
    
    def start = {
      logger.info(s"Starting session $id")
      stopWatchOrFail.start
    }
    
    def stop = {
      logger.info(s"Stopping session $id")
      stopWatchOrFail.stop
    }
    
  }

  case class Offer(
    offerId:Long = -1,
    qty:Long,
    qtyUnit: QtyUnit,
    price:Long,
    description:String
  )

  case object QtyUnit extends Enumeration {
    type QtyUnit = Value
    val MB = Value("MB")
    val minutes = Value("minutes")
  }
  
}
