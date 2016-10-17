package wallet

import java.io.File
import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.bitcoinj.core.{NetworkParameters, ECKey}
import org.bitcoinj.kits.WalletAppKit
import commons.Configuration.config
import wallet.WalletSupervisorService.GET_RECEIVING_ADDRESS

/**
  * Created by andrea on 17/10/16.
  */
class WalletSupervisorService extends Actor with LazyLogging {

  val networkParams = NetworkParameters.fromID(config.getString("wallet.net"))
  val walletFileName = config.getString("wallet.walletFile")

  val kit = new WalletAppKit(networkParams, new File("."), walletFileName) {
    override def onSetupCompleted() { wallet.importKey(new ECKey) }
  }

  def wallet = kit.wallet()

  override def preStart():Unit = {
    kit.startAsync()
  }

  def receive = {
    case GET_RECEIVING_ADDRESS =>
      sender() ! bytes2hex( wallet.currentReceiveAddress.getHash160 )

    case something => logger.info(s"Yo i got $something")
  }

  def bytes2hex(bytes:Array[Byte]):String = bytes.map("%02x".format(_)).mkString

}

object WalletSupervisorService {

  sealed trait MESSAGES
  case object GET_RECEIVING_ADDRESS extends MESSAGES

}
