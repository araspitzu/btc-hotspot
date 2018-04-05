package protocol

import java.time.LocalDateTime

import commons.Helpers.FutureOption
import protocol.domain.Invoice
import registry.DatabaseRegistry
import registry.SessionRepositoryRegistry._
import registry.OfferRepositoryRegistry._

import scala.concurrent.Future

trait InvoiceRepositoryComponent {

  val invoiceRepositoryImpl: InvoiceRepositoryImpl

}

class InvoiceRepositoryImpl extends DbSerializers {
  import DatabaseRegistry.database.database.profile.api._

  val db: Database = DatabaseRegistry.database.db

  class InvoiceTable(tag: Tag) extends Table[Invoice](tag, "INVOICES") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATETIME"))
    def expiresAt = column[LocalDateTime]("expiresAt", O.SqlType("DATETIME"))
    def paid = column[Boolean]("paid")
    def lnInvoice = column[String]("lnInvoice")
    def offerId = column[Option[Long]]("offerId")
    def sessionId = column[Option[Long]]("sessionId")

    def session = foreignKey("sessionFK", sessionId, sessionRepositoryImpl.sessionsTable)(_.id.?)
    //causes JdbcSQLException: Constraint "offerFK" already exists
    //def offer = foreignKey("offerFK", offerId, OfferRepositoryRegistry.offerRepositoryImpl.offersTable)(_.offerId.?)

    override def * = (id, createdAt, expiresAt, paid, lnInvoice, sessionId, offerId) <> (Invoice.tupled, Invoice.unapply)

  }

  val invoiceTable = TableQuery[InvoiceTable]

  def insert(invoice: Invoice): Future[Long] = db.run {
    (invoiceTable returning invoiceTable.map(_.id)) += invoice
  }

  def upsert(invoice: Invoice): FutureOption[Long] = db.run {
    (invoiceTable returning invoiceTable.map(_.id)).insertOrUpdate(invoice)
  }

  def invoiceById(id: Long): FutureOption[Invoice] = db.run {
    invoiceTable
      .filter(_.id === id)
      .result
      .headOption
  }

  def activeInvoicesBySessionId(sessionId: Long): Future[Seq[Invoice]] = db.run {
    invoiceTable
      .filter(_.sessionId === sessionId)
      //.join(offerRepositoryImpl.offersTable).on((s,o) => s.offerId.map(_ === o.offerId))
      .result
  }

}
