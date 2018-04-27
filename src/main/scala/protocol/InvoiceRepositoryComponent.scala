package protocol

import java.time.LocalDateTime

import commons.Helpers.FutureOption
import protocol.domain.Invoice
import scala.concurrent.Future

class InvoiceRepositoryImpl(val databaseComponent: DatabaseImpl, val sessionRepository: SessionRepositoryImpl) extends DbSerializers {
  import databaseComponent.database.profile.api._

  val db = databaseComponent.database.db

  class InvoiceTable(tag: Tag) extends Table[Invoice](tag, "INVOICES") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def createdAt = column[LocalDateTime]("createdAt", O.SqlType("DATETIME"))
    def expiresAt = column[LocalDateTime]("expiresAt", O.SqlType("DATETIME"))
    def paid = column[Boolean]("paid")
    def lnInvoice = column[String]("lnInvoice")
    def offerId = column[Option[Long]]("offerId")
    def sessionId = column[Option[Long]]("sessionId")

    def session = foreignKey("sessionFK", sessionId, sessionRepository.sessionsTable)(_.id.?)
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

  def allPaidInvoices(): Future[Seq[Invoice]] = db.run {
    invoiceTable
      .filter(_.paid === true)
      .result
  }

}
