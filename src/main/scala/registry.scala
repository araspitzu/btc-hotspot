import protocol.DatabaseComponent
import resources.{MiniPortal, PaymentChannelAPI}
import wallet.WalletServiceComponent


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

/**
  * Created by andrea on 09/12/16.
  */
package object registry {
  
  object MiniPortalRegistry
    extends MiniPortal
      with PaymentChannelAPI
      with WalletServiceComponent {
    
    override val walletService = new WalletService
  }
  
  object DatabaseRegistry extends DatabaseComponent {
    override val database = new Database
  }
  
}
