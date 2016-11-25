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

import protocol.domain.{QtyUnit, Quantity, Session, Offer}
import protocol.domain.QtyUnit._
import scala.collection.mutable

/**
  * Created by andrea on 17/11/16.
  */
object Repository {

  private val offerCache = new mutable.HashMap[String, Offer]()
  private val sessionOfferCache = new mutable.HashMap[Session, Option[Offer]]()
  private val sessionMacCache = new mutable.HashMap[String, Session]()

  val offer1 = Offer(
    qty = Quantity(10, minutes),
    price = 350000,
    description =  "10 minutes"
  )
  val offer2 = Offer(
    qty = Quantity(20, minutes),
    price = 450000,
    description =  "20 minutes"
  )
  val offer3 = Offer(
    qty = Quantity(30, minutes),
    price = 500000,
    description =  "30 minutes"
  )

  offerCache.put(offer1.offerId, offer1)
  offerCache.put(offer2.offerId, offer2)
  offerCache.put(offer3.offerId, offer3)


  def allOffers = offerCache.values.toSeq

  def offerById(offerId:String) = offerCache(offerId)

  def allSessions = sessionMacCache.values.toSeq

  def insertSessionForMac(session:Session, mac:String) = sessionMacCache.put(mac, session)

  def sessionByMac(mac:String) = sessionMacCache.get(mac)


}