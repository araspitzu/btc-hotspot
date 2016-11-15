package protocol

import protocol.domain.PriceUnit.PriceUnit

/**
  * Created by andrea on 15/11/16.
  */
package object domain {

  case class Offer(
    offerId:String,
    price:Price,
    description:String,
    paymentURI:String
  )

  case object PriceUnit extends Enumeration {
    type PriceUnit = Value
    val MB = Value("MB")
    val seconds = Value("seconds")
  }

  case class Price(
    value:Long,
    unit:PriceUnit
  )

}
