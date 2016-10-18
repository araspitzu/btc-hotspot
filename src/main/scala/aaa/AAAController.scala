package aaa

import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.slf4j.LazyLogging
import resources.CommonResource

/**
  * Created by andrea on 18/10/16.
  */
trait AAAController extends CommonResource with LazyLogging {


  def aaaRoute:Route = {
    (get & post & put) {
      path("aaa"){
        complete{
          logger.info(s"YEEEEEE")
          ""
        }
      }
    }
  }


}
