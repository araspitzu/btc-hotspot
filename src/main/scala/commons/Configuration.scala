package commons

import com.typesafe.config.ConfigFactory

/**
  * Created by andrea on 09/09/16.
  */
object Configuration {

  lazy val config = ConfigFactory.load()

}

