package resources

import akka.http.scaladsl.server.Route

/**
  * Created by andrea on 13/11/16.
  */
trait OffersAPI extends CommonResource{

  val offerMarshaller = json4sMarshaller[Offer]

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
