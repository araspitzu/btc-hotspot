package protocol

import org.joda.time.LocalDateTime
import protocol.domain.QtyUnit.QtyUnit

/**
  * Created by andrea on 15/11/16.
  */
package object domain {

  case class Session(
    id:String = java.util.UUID.randomUUID.toString,
    createdAt:LocalDateTime = LocalDateTime.now,
    clientMac:String,
    remainingUnits:Long = -1
  )

  case class Offer(
    offerId:String = java.util.UUID.randomUUID.toString,
    qty:Quantity,
    price:Long,
    description:String
  )

  case object QtyUnit extends Enumeration {
    type QtyUnit = Value
    val MB = Value("MB")
    val seconds = Value("seconds")
  }

  case class Quantity(
    value:Long,
    unit:QtyUnit
  )

}
