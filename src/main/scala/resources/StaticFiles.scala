package resources

import akka.http.scaladsl.server.Route
import commons.Configuration._

/**
  * Created by andrea on 19/10/16.
  */
trait StaticFiles extends CommonResource {

  val staticFilesDir = config.getString("miniportal.staticFilesDir")

  val staticFilesRoute:Route = getFromDirectory(staticFilesDir)

}
