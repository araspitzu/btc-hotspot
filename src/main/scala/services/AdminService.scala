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

import com.typesafe.scalalogging.LazyLogging
import protocol.SessionRepositoryImpl
import registry.{Registry, SessionRepositoryRegistry}
import wallet.{WalletServiceInterface, WalletServiceRegistry}

object AdminServiceRegistry extends Registry with AdminServiceComponent {
  
  val adminService = new AdminServiceImpl()
  
}

trait AdminServiceComponent {
  
  val adminService:AdminService
  
}

trait AdminService {
  
  def walletBalance():Long
  
}

class AdminServiceImpl(dependencies:{
  val sessionRepository: SessionRepositoryImpl
  val offerService:OfferServiceInterface
  val walletService: WalletServiceInterface
}) extends AdminService with LazyLogging {
  import dependencies.walletService._
  import dependencies.sessionRepository._
  import dependencies.offerService._
  
  def this() = this(new {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val offerService:OfferServiceInterface = OfferServiceRegistry.offerService
    val walletService: WalletServiceInterface = WalletServiceRegistry.walletService
  })
  
  override def walletBalance():Long = getBalance
  
  
}
