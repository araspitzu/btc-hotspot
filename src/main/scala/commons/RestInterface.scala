package commons

import akka.http.scaladsl.server.Route
import resources.{SharedMemoryResource, CounterResource}

/**
  * Created by andrea on 09/09/16.
  */
trait RestInterface extends CounterResource with SharedMemoryResource {

  val routes: Route = counterRoute ~ route

}
