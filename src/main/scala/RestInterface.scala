import akka.http.scaladsl.server.Route
import resources.{CounterResource, CommonResource}

import scala.concurrent.ExecutionContext

/**
  * Created by andrea on 09/09/16.
  */
trait RestInterface extends Resource {

  implicit def executionContext: ExecutionContext

  val routes: Route = counterRoute

}

trait Resource extends CounterResource

