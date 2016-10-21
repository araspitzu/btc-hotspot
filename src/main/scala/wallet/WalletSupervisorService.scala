package wallet

import java.io.File
import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.bitcoinj.core.{Coin, NetworkParameters, ECKey}
import org.bitcoinj.kits.WalletAppKit
import commons.Configuration.config
import org.bitcoinj.protocols.payments.PaymentProtocol
import wallet.WalletSupervisorService.{PAYMENT_REQUEST, GET_RECEIVING_ADDRESS}

/**
  * Created by andrea on 17/10/16.
  */
class WalletSupervisorService extends Actor with LazyLogging {

  val network = NetworkParameters.fromID(config.getString("wallet.net"))
  val walletFileName = config.getString("wallet.walletFile")

  val kit = new WalletAppKit(network, new File("."), walletFileName) {
    override def onSetupCompleted() { wallet.importKey(new ECKey) }
  }

  def networkParams = kit.params
  def wallet = kit.wallet

  override def preStart():Unit = {
    kit.startAsync()
  }



  def receive = {
    case GET_RECEIVING_ADDRESS =>
      sender() ! bytes2hex( wallet.currentReceiveAddress.getHash160 )

    // Spawn new actor to handle payment request?
    case PAYMENT_REQUEST(sessionId) => {

      logger.info(s"Issuing payment request for session $sessionId")

      sender() ! PaymentProtocol.createPaymentRequest(
        networkParams,
        Coin.valueOf(35000),
        wallet.currentReceiveAddress,
        s"Please pay 0.002 for using session $sessionId",
        s"http://127.0.0.1:8081/pay/$sessionId",
        Array.emptyByteArray
      ).build()

    }

    case something => logger.warn(s"Yo i got $something")
  }

  def bytes2hex(bytes:Array[Byte]):String = bytes.map("%02x ".format(_)).mkString

}

object WalletSupervisorService {

  sealed trait MESSAGES
  case object GET_RECEIVING_ADDRESS extends MESSAGES
  case class PAYMENT_REQUEST(sessionId:String) extends MESSAGES

}
