package protocol

import com.typesafe.scalalogging.LazyLogging
import commons.Configuration.DbConfig.{ configPath, jdbcUrl, webUI }
import commons.Helpers
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DatabaseImpl extends LazyLogging {

  val database: DatabaseConfig[JdbcProfile] = {
    logger.info(s"Opening database for conf '$configPath' @ $jdbcUrl")

    if (webUI) {
      logger.info(s"Creating web ui @ localhost:8888")
      org.h2.tools.Server.createWebServer("-webAllowOthers", "-webPort", "8888").start()
    }

    DatabaseConfig.forConfig[JdbcProfile](configPath)
  }

  Helpers.addShutDownHook {
    logger.info("Shutting down db")
    Await.result(database.db.shutdown, Duration(2, "seconds"))
  }

}
