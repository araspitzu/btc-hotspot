package wallet

import java.io.File

import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration.WalletConfig._
import commons.Configuration.MiniPortalConfig._
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos.{PaymentACK, PaymentRequest}
import org.bitcoinj.core.TransactionBroadcast.ProgressCallback
import org.bitcoinj.core.{Transaction, Coin, ECKey}
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.protocols.payments.PaymentProtocol
/**
  * Created by andrea on 16/11/16.
  */
class WalletService extends LazyLogging {

  val file = new File(walletDir)

  val kit = new WalletAppKit(network, file, walletFileName) {
    override def onSetupCompleted() { wallet.importKey(new ECKey) }
  }

  def networkParams = kit.params
  def peerGroup = kit.peerGroup
  def wallet = kit.wallet

  def receivingAddress:String = bytes2hex( wallet.currentReceiveAddress.getHash160 )

  def bytes2hex(bytes:Array[Byte]):String = bytes.map("%02x ".format(_)).mkString

  def generatePaymentRequest(sessionId:String, offerId:String):PaymentRequest = {
    logger.info(s"Issuing payment request for session $sessionId")

    val owedSatoshis = 35000

    PaymentProtocol.createPaymentRequest(
      networkParams,
      Coin.valueOf(owedSatoshis),
      wallet.currentReceiveAddress,
      s"Please pay $owedSatoshis satoshis for using session $sessionId",
      s"http://$miniPortalHost:$miniPortalPort/api/pay/$sessionId",
      Array.emptyByteArray
    ).build()
  }

  def payment(payment:Protos.Payment):Protos.PaymentACK = {

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

    PaymentACK.newBuilder.build

  }

}
