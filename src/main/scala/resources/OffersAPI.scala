package resources

import akka.http.scaladsl.server.Route
import commons.JsonSupport


/**
  * Created by andrea on 13/11/16.
  */
trait OffersAPI extends CommonResource with JsonSupport {


  def offersRoute:Route = get {
    path("offers"){
      complete(Offer(2.43, "Just an offer"))
    }
  }

  case class Offer(
     price:Double,
     description:String
  )

}
