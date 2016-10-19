package resources

import akka.http.scaladsl.server.Route
/**
  * Created by andrea on 19/10/16.
  */
trait StaticFiles extends CommonResource {

  val staticFilesRoute:Route = {
    path("resources" / Segment) { fileName =>
      getFromResource(fileName)
    }
  }

}
