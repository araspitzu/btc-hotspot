package wallet

import java.io.File
import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.bitcoin.protocols.payments.Protos
import org.bitcoinj.core.TransactionBroadcast._
import org.bitcoinj.core._
import org.bitcoinj.kits.WalletAppKit
import commons.Configuration.config
import org.bitcoinj.protocols.payments.PaymentProtocol
import wallet.WalletSupervisorService.{PAYMENT_ACK, PAYMENT, PAYMENT_REQUEST, GET_RECEIVING_ADDRESS}
import commons.Helpers.ScalaConversions._

/**
  * Created by andrea on 17/10/16.
  */
class WalletSupervisorService extends Actor with LazyLogging {

  val network = NetworkParameters.fromID(config.getString("wallet.net"))
  val walletFileName = config.getString("wallet.walletFile")

  val file = new File(".")

  logger.info(s"Using wallet dir ${file.getAbsolutePath}")
  val kit = new WalletAppKit(network, file, walletFileName) {
    override def onSetupCompleted() { wallet.importKey(new ECKey) }


  }

  def networkParams = kit.params
  def peerGroup = kit.peerGroup
  def wallet = kit.wallet

  override def preStart():Unit = {
    if(config.getBoolean("wallet.enabled"))
      kit.startAsync()
  }



  def receive = {
    case GET_RECEIVING_ADDRESS =>
      sender() ! bytes2hex( wallet.currentReceiveAddress.getHash160 )

    // Spawn new actor to handle payment request?
    case PAYMENT_REQUEST(sessionId) => {

      logger.info(s"Issuing payment request for session $sessionId")

      val owedSatoshis = 35000

      sender() ! PaymentProtocol.createPaymentRequest(
        networkParams,
        Coin.valueOf(owedSatoshis),
        wallet.currentReceiveAddress,
        s"Please pay $owedSatoshis satoshis for using session $sessionId",
        s"http://127.0.0.1:8081/pay/$sessionId",
        Array.emptyByteArray
      ).build()

    }

    //Broadcast transaction(s) and according to confidence level
    //respond OK/KO to sender actor
    case PAYMENT(payment) => {

      for( i <- 0 to (payment.getTransactionsCount - 1) ) yield {
        val txBytes = payment.getTransactions(i).toByteArray
        val tx = new Transaction(networkParams, txBytes)
        val broadcast = peerGroup.broadcastTransaction(tx)

        broadcast.setProgressCallback(new ProgressCallback {
          override def onBroadcastProgress(progress: Double): Unit = {
            logger.info(s"TX ${tx.getHashAsString} broadcast at ${progress * 100}%")
          }
        })


      }

      sender() ! PAYMENT_ACK
    }

    case something => logger.warn(s"Yo i got $something")
  }

  def bytes2hex(bytes:Array[Byte]):String = bytes.map("%02x ".format(_)).mkString

}

object WalletSupervisorService {

  sealed trait MESSAGES
  case object GET_RECEIVING_ADDRESS extends MESSAGES
  case class PAYMENT_REQUEST(sessionId:String) extends MESSAGES
  case class PAYMENT(payment:Protos.Payment) extends MESSAGES
  case object PAYMENT_ACK extends MESSAGES

}
