package protocol

import protocol.domain.{PriceUnit, Quantity, Session, Offer}
import protocol.domain.PriceUnit._
import scala.collection.mutable

/**
  * Created by andrea on 17/11/16.
  */
object Repository {

  private val offerCache = new mutable.HashMap[String, Offer]()

  val offer1 = Offer(
    qty = Quantity(120, seconds),
    price = 3500,
    description =  "120 seconds"
  )

  val offer2 = Offer(
    qty = Quantity(5000, MB),
    price = 3500,
    description =  "5000 megabytes"
  )

  offerCache.put(offer1.offerId, offer1)
  offerCache.put(offer2.offerId, offer2)

  private val sessionOfferCache = new mutable.HashMap[Session, Option[Offer]]()

  private val sessionMacCache = new mutable.HashMap[String, Session]()

  def allOffers = offerCache.values.toSeq

  def insertSessionForMac(session:Session, mac:String) = sessionMacCache.put(mac, session)

  def sessionByMac(mac:String) = sessionMacCache(mac)


}