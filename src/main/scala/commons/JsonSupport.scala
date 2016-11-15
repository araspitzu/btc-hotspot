package commons

import java.text.SimpleDateFormat

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{native, Formats, DefaultFormats}
import org.json4s.ext.JodaTimeSerializers

/**
  * Created by andrea on 15/11/16.
  */
trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization

  implicit def json4sFormats: Formats = DefaultFormats//customDateFormat ++ JodaTimeSerializers.all

//  val customDateFormat = new DefaultFormats {
//    override def dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
//  }
  
}
