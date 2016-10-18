package resources

import akka.http.scaladsl.server.Route

/**
  * Created by andrea on 09/09/16.
  */
trait MiniPortal extends WelcomeController {

  val miniportalRoute: Route = welcomeRoute

}
