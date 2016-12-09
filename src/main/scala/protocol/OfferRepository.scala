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

import commons.Helpers.FutureOption
import protocol.domain.{Offer, QtyUnit}
import protocol.domain.QtyUnit.QtyUnit
import slick.driver.H2Driver.api._
import registry.DatabaseRegistry._
import scala.concurrent.Future

/**
  * Created by andrea on 09/12/16.
  */
object OfferRepository {
  this:DatabaseComponent =>
  
  import database.db
  
  class OfferTable(tag:Tag) extends Table[Offer](tag,"OFFERS"){
    
    implicit val qtyUnitMapper = MappedColumnType.base[QtyUnit, String](
      e => e.toString,
      s => QtyUnit.withName(s)
    )
    
    def offerId = column[Long]("offerId", O.PrimaryKey, O.AutoInc)
    def qty = column[Long]("qty")
    def qtyUnit = column[QtyUnit]("qtyUnit")
    def price = column[Long]("price")
    def description = column[String]("description")
    
    override def * = (offerId, qty, qtyUnit, price, description) <> (Offer.tupled, Offer.unapply)
  }
  
  val offersTable = TableQuery[OfferTable]
  
  def byId(id:Long):FutureOption[Offer] = db.run {
    offersTable
      .filter(_.offerId === id)
      .map(identity)
      .result
      .headOption
  }
  
  def insert(offer: Offer):Future[Int] = db.run {
    offersTable
      .insertOrUpdate(offer)
  }
  
  def allOffers:Future[Seq[Offer]] = db.run {
    offersTable.map(identity).result
  }
  
}

