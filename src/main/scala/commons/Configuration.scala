package commons

import org.bitcoinj.core.NetworkParameters

/**
  * Created by andrea on 09/09/16.
  */
object Configuration {

  lazy val config = com.typesafe.config.ConfigFactory.load()

  object WalletConfig {
    val isEnabled = config.getBoolean("wallet.enabled")
    val network:NetworkParameters = NetworkParameters.fromID(config.getString("wallet.net"))
    val walletFileName = config.getString("wallet.walletFile")
    val walletDir = config.getString("wallet.walletDir")
  }

  object MiniPortalConfig {
    val staticFilesDir = config.getString("miniportal.staticFilesDir")
    val miniPortalHost = config.getString("miniportal.host")
    val miniPortalPort = config.getInt("miniportal.port")
    val miniPortalIndex = config.getString("miniportal.index")
  }
}

