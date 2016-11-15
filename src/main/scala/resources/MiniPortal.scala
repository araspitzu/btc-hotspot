package resources

import akka.http.scaladsl.server.Route

/**
  * Created by andrea on 09/09/16.
  */
trait MiniPortal extends PaymentChannelAPI with StaticFiles with OffersAPI {

  val miniportalRoute: Route =
    paymentChannelRoute ~
    staticFilesRoute ~
    offersRoute ~
    entryPointRoute  //must stay in the last position because it matches any request

}
