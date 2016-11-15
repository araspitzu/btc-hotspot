package commons

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{native, Formats, DefaultFormats}

/**
  * Created by andrea on 15/11/16.
  */
trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization

  //Append custom formats here
  implicit def json4sFormats: Formats = DefaultFormats

}
