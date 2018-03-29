package protocol

import java.time.LocalDateTime

import protocol.domain.Invoice
import registry.{DatabaseRegistry, OfferRepositoryRegistry, SessionRepositoryRegistry}

trait InvoiceRepository {

  val invoiceRepository: InvoiceRepositoryImpl

}

//TODO Use dependency injection
class InvoiceRepositoryImpl extends DbSerializers {
  import DatabaseRegistry.database.database.profile.api._

  lazy val db: Database = DatabaseRegistry.database.db

  class InvoiceTable(tag: Tag) extends Table[Invoice](tag, "INVOICES") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATETIME")) //mapped via java.time.LocalDateTime -> java.sql.Timestamp -> DATATYPE(DATETIME)
    def paid = column[Boolean]("paid")
    def lnInvoice = column[String]("lnInvoice")
    def sessionId = column[Option[Long]]("sessionId")
    def offerId = column[Option[Long]]("offerId")


    def session = foreignKey("sessionFK", sessionId, SessionRepositoryRegistry.sessionRepositoryImpl.sessionsTable)(_.id.?)
    def offer = foreignKey("offerFK", offerId, OfferRepositoryRegistry.offerRepositoryImpl.offersTable)(_.offerId.?)

    override def * = (id, createdAt, paid, lnInvoice, sessionId, offerId) <> (Invoice.tupled, Invoice.unapply)

  }

  val invoiceTable = TableQuery[InvoiceTable]



}
