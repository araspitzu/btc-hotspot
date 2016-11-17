package resources

import akka.http.scaladsl.server.Route
import commons.JsonSupport
import protocol.domain.{PriceUnit, Price, Offer}


/**
  * Created by andrea on 13/11/16.
  */
trait OffersAPI extends CommonResource with JsonSupport {


  def offersRoute:Route = get {
    path("api" / "offers" / Segment){ offerId =>
      complete(Offer(
        offerId = java.util.UUID.randomUUID.toString,
        price = Price(2345, PriceUnit.MB),
        description = "Buy 2345 MB!",
        paymentURI = s"bitcoin:r=/api/pay/session?offerId=$offerId"
      ))
    }
  }

}
