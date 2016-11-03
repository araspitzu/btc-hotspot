package commons

/**
  * Created by andrea on 09/09/16.
  */
object Configuration {

  lazy val config = com.typesafe.config.ConfigFactory.load()

}

