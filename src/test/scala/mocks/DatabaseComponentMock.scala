package mocks

import protocol.DatabaseComponent

object DatabaseComponentMock extends DatabaseComponent {
  override val database: DatabaseImpl = ???
}
