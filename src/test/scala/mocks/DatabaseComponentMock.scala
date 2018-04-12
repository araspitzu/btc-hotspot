package mocks

import commons.Configuration.DbConfig.configPath
import protocol.DatabaseComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object DatabaseComponentMock extends DatabaseComponent {
  override val database: DatabaseImpl = new DatabaseImpl {

    override val database: DatabaseConfig[JdbcProfile] = {

      DatabaseConfig.forConfig[JdbcProfile](configPath)

    }

  }

  registry.setupDb(this)

}
