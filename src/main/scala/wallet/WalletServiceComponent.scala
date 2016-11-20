package wallet

import java.io.File

import com.google.protobuf.ByteString
import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Configuration.WalletConfig._
import commons.Configuration.MiniPortalConfig._
import iptables.IpTablesService
import org.bitcoin.protocols.payments.Protos
import org.bitcoin.protocols.payments.Protos.PaymentRequest
import org.bitcoinj.core.TransactionBroadcast.ProgressCallback
import org.bitcoinj.core._
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.protocols.payments.PaymentProtocol
import org.bitcoinj.wallet.KeyChain.KeyPurpose
import protocol.Repository
import protocol.domain.{Offer, Session}
import scala.collection.JavaConverters._
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
     .setUserAgent("paypercom", "0.0.1-alpha")

    if(isEnabled)
      kit.startAsync

    def networkParams:NetworkParameters = kit.params
    def peerGroup:PeerGroup = kit.peerGroup
    def wallet:org.bitcoinj.wallet.Wallet = kit.wallet
    def ownerReceivingAddress:Address = wallet.currentAddress(KeyPurpose.RECEIVE_FUNDS)
    def ppcReceivingAddress:Address = Address.fromBase58(networkParams, ppcAddress)

    def receivingAddress: String = bytes2hex(wallet.currentReceiveAddress.getHash160)

    def bytes2hex(bytes: Array[Byte]): String = bytes.map("%02x ".format(_)).mkString

    def generatePaymentRequest(session: Session, offerId: String): Future[PaymentRequest] = Future {
      logger.info(s"Issuing payment request for session ${session.id} and offer $offerId")

      val Some(offer) = Repository.allOffers.find(_.offerId == offerId)

      PaymentProtocol.createPaymentRequest(
        networkParams,
        outputsForOffer(offer).asJava,
        s"Please pay ${offer.price} satoshis for ${offer.description}",
        s"http://$miniPortalHost:$miniPortalPort/api/pay/${session.id}",
        Array.emptyByteArray
      ).build

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

    def p2pubKeyHash(value:Long, to:Address):ByteString = {
      ByteString.copyFrom(new TransactionOutput(
        networkParams,
        null,
        Coin.valueOf(value),
        to
      ).getScriptBytes)
    }

    def outputsForOffer(offer:Offer):List[Protos.Output] = {
      def outputBuilder = Protos.Output.newBuilder

      val ppcFeeSatoshis:Long = offer.price / 100  //1% beware of becoming dust
      val offerSatoshis:Long = offer.price - ppcFeeSatoshis


      val ppcOutput =
        outputBuilder
          .setAmount(ppcFeeSatoshis)
          .setScript(p2pubKeyHash(
            value = ppcFeeSatoshis,
            to = ppcReceivingAddress
          ))
          .build

      val ownerOutput =
        outputBuilder
          .setAmount(offerSatoshis)
          .setScript(p2pubKeyHash(
            offerSatoshis,
            to = ownerReceivingAddress
          ))
          .build

      List(ppcOutput, ownerOutput)
    }




  }


}
