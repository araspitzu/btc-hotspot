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

package commons

import org.bitcoinj.core.NetworkParameters

object Configuration {

  lazy val config = com.typesafe.config.ConfigFactory.load()

  lazy val env = config.getString("env")

  object WalletConfig {
    val isEnabled = config.getBoolean("wallet.enabled")
    val network: NetworkParameters = NetworkParameters.fromID(config.getString("wallet.net"))
    val walletFileName = config.getString("wallet.walletFile")
    val walletDir = config.getString("wallet.walletDir")
  }

  object EclairConfig {
    val host = config.getString("eclair.host")
    val port = config.getString("eclair.port")
    val apiPassword = config.getString("eclair.apiToken")
    val protocol = if (config.getBoolean("eclair.useSsl")) "https" else "http"
  }

  object MiniPortalConfig {
    val miniportalStaticFilesDir = config.getString("miniportal.staticFilesDir")
    val miniPortalHost = config.getString("miniportal.host")
    val miniPortalPort = config.getInt("miniportal.port")
    val miniPortalIndex = config.getString("miniportal.index")
  }

  object AdminPanelConfig {
    val adminPanelStaticFilesDir = config.getString("admin_panel.staticFilesDir")
    val adminPanelHost = config.getString("admin_panel.host")
    val adminPanelPort = config.getInt("admin_panel.port")
    val adminPanelIndex = config.getString("admin_panel.index")
  }

  object DbConfig {
    val configPath = "database"
    val jdbcUrl = config.getString("database.db.url")
    val webUI = config.getBoolean("database.webUI")
  }

  object EmailConfig {
    val smtpServer = config.getString("mail.smpt_server")
    val port = config.getInt("mail.port")
    val username = config.getString("mail.username")
    val password = config.getString("mail.password")
  }

  object NetworkConfig {
    val uplinkInterfaceName = config.getString("network.iface_uplink")
    val downlinkInterfaceName = config.getString("network.iface_downlink")
  }

}

