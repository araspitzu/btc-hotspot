package wallet

import java.io.{IOException, File}

import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration.WalletConfig._
import commons.Configuration.MiniPortalConfig._
import iptables.IpTablesService
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos.{PaymentRequest}
import org.bitcoinj.core.TransactionBroadcast.ProgressCallback
import org.bitcoinj.core.{Transaction, Coin, ECKey}
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.protocols.payments.PaymentProtocol
import protocol.domain.Session
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by andrea on 16/11/16.
  */
trait WalletServiceComponent extends LazyLogging {

  val walletService: WalletService

  class WalletService {

    val file = new File(walletDir)

    val kit = new WalletAppKit(network, file, walletFileName) {
      override def onSetupCompleted() {
        wallet.importKey(new ECKey)
      }
    }.setAutoStop(true)

    if(isEnabled)
      kit.startAsync

    def networkParams = kit.params
    def peerGroup = kit.peerGroup
    def wallet = kit.wallet

    def receivingAddress: String = bytes2hex(wallet.currentReceiveAddress.getHash160)

    def bytes2hex(bytes: Array[Byte]): String = bytes.map("%02x ".format(_)).mkString

    def generatePaymentRequest(session: Session, offerId: String): Future[PaymentRequest] = Future {
      logger.info(s"Issuing payment request for session ${session.id} and offer $offerId")

      val owedSatoshis = 35000 //offer.price

      PaymentProtocol.createPaymentRequest(
        networkParams,
        Coin.valueOf(owedSatoshis),
        wallet.currentReceiveAddress,
        s"Please pay $owedSatoshis satoshis for using session ${session.id}",
        s"http://$miniPortalHost:$miniPortalPort/api/pay/${session.id}",
        Array.emptyByteArray
      ).build()
    }

    def validatePayment(session: Session, payment: Protos.Payment): Future[Protos.PaymentACK] = Future {

      for (i <- 0 to (payment.getTransactionsCount - 1)) yield {
        val txBytes = payment.getTransactions(i).toByteArray
        val tx = new Transaction(networkParams, txBytes)
        val broadcast = peerGroup.broadcastTransaction(tx)

        broadcast.setProgressCallback(new ProgressCallback {
          override def onBroadcastProgress(progress: Double): Unit = {
            logger.info(s"TX ${tx.getHashAsString} broadcast at ${progress * 100}%")
          }
        })

      }

      IpTablesService.enableClient(session.clientMac)

      PaymentProtocol.createPaymentAck(payment, s"Enjoy session your session!")
    }
  }

}
