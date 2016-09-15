package commons

import akka.http.scaladsl.server.Route
import resources.CounterResource

/**
  * Created by andrea on 09/09/16.
  */
trait RestInterface extends CounterResource {

  val routes: Route = counterRoute // ~ otherRoute

}
