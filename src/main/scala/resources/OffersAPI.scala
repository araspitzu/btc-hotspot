package resources

import akka.http.scaladsl.server.Route
import commons.JsonSupport
import protocol.Repository
import protocol.webDto.WebOfferDto

/**
  * Created by andrea on 13/11/16.
  */
trait OffersAPI extends CommonResource with JsonSupport {


  def offersRoute:Route = get {
    path("api" / "offers" / Segment){ offerId =>
      complete(Repository.allOffers.map(WebOfferDto(_)))
    }
  }

}
