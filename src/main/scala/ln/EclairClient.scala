package ln

import scala.concurrent.Future

trait EclairClient {

  def connect(uri: String): Future[String]

  def openChannel(msat: Long, peer: String): Future[String]

  def getInvoice(msat: Long, msg: String): Future[String]

  def checkInvoice(invoice: String): Future[Boolean]

  def sendTo(lnInvoice: String, msat: Long ): Future[String]

}
