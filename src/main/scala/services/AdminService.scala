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
import protocol.domain.Session
import registry.{Registry, SessionRepositoryRegistry}
import wallet.{WalletServiceInterface, WalletServiceRegistry}
import commons.AppExecutionContextRegistry.context._
import protocol.webDto.{BitcoinTransactionDto, WithdrawTransactionData}

import scala.concurrent.Future

object AdminServiceRegistry extends Registry with AdminServiceComponent {
  
  val adminService = new AdminServiceImpl()
  
}

trait AdminServiceComponent {
  
  val adminService:AdminService
  
}

trait AdminService {
  
  def walletBalance():Long
  
  def activeSessions():Future[Seq[Session]]
  
  def allSessions():Future[Seq[Session]]
  
  def transactions():Seq[BitcoinTransactionDto]
  
  def withdraw(wtd: WithdrawTransactionData):Future[String]
  
}

class AdminServiceImpl(dependencies:{
  val sessionRepository: SessionRepositoryImpl
  val sessionService: SessionServiceInterface
  val offerService:OfferServiceInterface
  val walletService: WalletServiceInterface
}) extends AdminService with LazyLogging {
  
  private def sessionRepository = dependencies.sessionRepository
  private def sessionService = dependencies.sessionService
  private def walletService = dependencies.walletService
  
  
  def this() = this(new {
    val sessionRepository: SessionRepositoryImpl = SessionRepositoryRegistry.sessionRepositoryImpl
    val sessionService: SessionServiceInterface = SessionServiceRegistry.sessionService
    val offerService:OfferServiceInterface = OfferServiceRegistry.offerService
    val walletService: WalletServiceInterface = WalletServiceRegistry.walletService
  })
  
  override def walletBalance():Long = walletService.getBalance
  
  override def activeSessions():Future[Seq[Session]] = {
    val activeSessionIds = sessionService.activeSessionIds
    for {
       activeSessions <- sessionRepository.byIdSet(activeSessionIds.toSet)
    } yield activeSessions
  }
  
  override def allSessions():Future[Seq[Session]] = sessionRepository.allSession
  
  override def transactions():Seq[BitcoinTransactionDto] = {
    walletService.getTransactions.map(BitcoinTransactionDto(_))
  }
  
  override def withdraw(wtd: WithdrawTransactionData):Future[String] =  {
    walletService.createSpendingTx(wtd.address, wtd.amount)
  }
  
}
