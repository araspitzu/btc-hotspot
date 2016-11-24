package protocol

import protocol.domain.{QtyUnit, Quantity, Session, Offer}
import protocol.domain.QtyUnit._
import scala.collection.mutable

/**
  * Created by andrea on 17/11/16.
  */
object Repository {

  private val offerCache = new mutable.HashMap[String, Offer]()
  private val sessionOfferCache = new mutable.HashMap[Session, Option[Offer]]()
  private val sessionMacCache = new mutable.HashMap[String, Session]()

  val offer1 = Offer(
    qty = Quantity(120, minutes),
    price = 350000,
    description =  "120 seconds"
  )
  val offer2 = Offer(
    qty = Quantity(5000, MB),
    price = 350000,
    description =  "5000 megabytes"
  )
  val offer3 = Offer(
    qty = Quantity(50000, MB),
    price = 500000,
    description =  "50000 megabytes"
  )

  offerCache.put(offer1.offerId, offer1)
  offerCache.put(offer2.offerId, offer2)
  offerCache.put(offer3.offerId, offer3)


  def allOffers = offerCache.values.toSeq

  def offerById(offerId:String) = offerCache(offerId)

  def allSessions = sessionMacCache.values.toSeq

  def insertSessionForMac(session:Session, mac:String) = sessionMacCache.put(mac, session)

  def sessionByMac(mac:String) = sessionMacCache.get(mac)


}