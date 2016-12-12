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

package services

import commons.Helpers.FutureOption
import protocol.OfferRepository
import protocol.domain.Offer

import scala.concurrent.Future

/**
  * Created by andrea on 27/11/16.
  */
object OfferService {

  def allOffers:Future[Seq[Offer]] = {
    OfferRepository.allOffers
  }
  
  def offerById(id:Long):FutureOption[Offer] = OfferRepository.byId(id)
  

}
