package commons

import akka.http.scaladsl.server.Route
import resources.{WelcomeController, CounterResource}

/**
  * Created by andrea on 09/09/16.
  */
trait RestInterface extends CounterResource with WelcomeController {

  val routes: Route = counterRoute ~ route

}
