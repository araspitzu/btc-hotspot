package service

import java.time.LocalDateTime

import commons.Helpers._
import fr.acinq.bitcoin.Crypto._
import fr.acinq.bitcoin.Satoshi
import ln.EclairClientImpl
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import protocol.InvoiceRepositoryImpl
import wallet.LightningServiceImpl

import scala.concurrent.ExecutionContext.Implicits.global

class WalletServiceSpecs extends Specification with Mockito {

  trait mockedScope extends Scope {

    val invoiceRepository = mock[InvoiceRepositoryImpl]
    val eclairClient = mock[EclairClientImpl]

  }

  "WalletService" should {

    "decode a lightning invoice" in new mockedScope {

      val walletService = new LightningServiceImpl(this)

      val lnInvoice = "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4jsxqzpuaztrnwngzn3kdzw5hydlzf03qdgm2hdq27cqv3agm2awhz5se903vruatfhq77w3ls4evs3ch9zw97j25emudupq63nyw24cg27h2rspfj9srp"
      val decodedInvoice = walletService.lnInvoiceToLightningInvoice(lnInvoice)

      decodedInvoice.value === Satoshi(250000)
      decodedInvoice.date === LocalDateTime.parse("2017-06-01T12:57:38")
      decodedInvoice.testnet must beFalse
      decodedInvoice.hash === "3cd6ef07744040556e01be64f68fd9e1565fb47d78c42308b1ee005aca5a0d86"

      val lnInvoice1 = "lntb15u1pdwxw8spp566lefws7x6al5xwga5mvkl0rman26dpz8phv84ne2587dlef5pmsdpd2pkx2ctnv5sxw6tkv5sx6efqwdhk6efqd4ekzar0wd5xjxqyz5vqjf8pg09wzy9mp8kedh3vvyhswmh5nxtjm7tc8xymyp72ue75lv0y3eqmtvlwtyhjhwh75f7tpf7rf2z5p33ctenxfy9fdhvwplxcztqpquz8ye"

      val decodedInvoice1 = walletService.lnInvoiceToLightningInvoice(lnInvoice1)

      decodedInvoice1.value === Satoshi(1500)
      decodedInvoice1.date === LocalDateTime.parse("2018-04-27T16:51:28")
      decodedInvoice1.testnet must beTrue

    }

  }

}
