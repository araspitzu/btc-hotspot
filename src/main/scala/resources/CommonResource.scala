package resources

import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext

/**
  * Created by andrea on 09/09/16.
  */
trait CommonResource extends Directives with Json4sSupport {

  implicit def executionContext: ExecutionContext

}
