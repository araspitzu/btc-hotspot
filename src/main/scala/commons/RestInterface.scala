package commons

import akka.http.scaladsl.server.Route
import resources.WelcomeController

/**
  * Created by andrea on 09/09/16.
  */
trait RestInterface extends WelcomeController {

  val routes: Route = route

}
