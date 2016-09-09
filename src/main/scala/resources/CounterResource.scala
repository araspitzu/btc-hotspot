package resources

import akka.http.scaladsl.server.Route

/**
  * Created by andrea on 09/09/16.
  */
trait CounterResource extends CommonResource {

  def counterRoute: Route = path("counter") {
    get {
      complete("Yo")
    }
  }

}
