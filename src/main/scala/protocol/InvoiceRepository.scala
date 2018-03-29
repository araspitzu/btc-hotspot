package protocol

import protocol.domain.Invoice
import registry.DatabaseRegistry

trait InvoiceRepository {

}

//TODO Use dependency injection
class InvoiceRepositoryImpl {
  import DatabaseRegistry.database.database.profile.api._

  lazy val db: Database = DatabaseRegistry.database.db

//  class InvoiceTable(tag: Tag) extends Table[Invoice](tag, "INVOICES") {
//
//  }

}
