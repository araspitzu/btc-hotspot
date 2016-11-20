package commons

import org.bitcoinj.core.NetworkParameters

/**
  * Created by andrea on 09/09/16.
  */
object Configuration {

  lazy val config = com.typesafe.config.ConfigFactory.load()

  lazy val env = config.getString("env")

  object WalletConfig {
    val isEnabled = config.getBoolean(s"wallet.$env.enabled")
    val network:NetworkParameters = NetworkParameters.fromID(config.getString(s"wallet.$env.net"))
    val walletFileName = config.getString(s"wallet.$env.walletFile")
    val walletDir = config.getString(s"wallet.$env.walletDir")
    val ppcAddress = config.getString(s"wallet.$env.ppcAddress")
  }

  object MiniPortalConfig {
    val staticFilesDir = config.getString(s"miniportal.$env.staticFilesDir")
    val miniPortalHost = config.getString(s"miniportal.$env.host")
    val miniPortalPort = config.getInt(s"miniportal.$env.port")
    val miniPortalIndex = config.getString(s"miniportal.$env.index")
  }
}

