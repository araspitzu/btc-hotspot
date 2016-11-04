package resources

import akka.http.scaladsl.server.Route

/**
  * Created by andrea on 09/09/16.
  */
trait MiniPortal extends PaymentChannelAPI with StaticFiles {

  val miniportalRoute: Route = paymentChannelRoute ~ staticFilesRoute ~ resourceRoute

}
