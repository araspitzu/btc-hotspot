package ln

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{ BasicHttpCredentials, `Content-Type` }
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpMethods._
import commons.AppExecutionContextRegistry.context._
import org.json4s.native._
import org.json4s.native.Serialization._
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration.EclairConfig
import commons.JsonSupport
import ln.model.{ JsonRPCRequest, JsonRPCResponse }
import org.json4s.JsonAST.{ JBool, JString }

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._

trait EclairClient {

  def connect(uri: String): Future[String]

  def openChannel(msat: Long, peer: String): Future[String]

  def getInvoice(msat: Long, msg: String): Future[String]

  def checkInvoice(invoice: String): Future[Boolean]

  def sendTo(lnInvoice: String, msat: Long): Future[String]

}

class EclairClientImpl extends EclairClient with JsonSupport with LazyLogging {

  val eclairHost = EclairConfig.host
  val eclairPort = EclairConfig.port
  val apiPassword = EclairConfig.apiPassword

  override def connect(uri: String): Future[String] = ???

  override def openChannel(msat: Long, peer: String): Future[String] = ???

  override def getInvoice(msat: Long, msg: String): Future[String] = {
    rpcCall(JsonRPCRequest(
      method = "receive",
      params = Seq(msat, msg)
    )).map(_.result match {
      case JString(lnInvoice) => lnInvoice
      case _                  => throw new IllegalStateException("Unable to get LN invoice")
    })
  }

  override def checkInvoice(invoiceHash: String): Future[Boolean] = {
    rpcCall(JsonRPCRequest(
      method = "checkinvoice",
      params = invoiceHash :: Nil
    )).map(_.result match {
      case JBool(isPaid) => isPaid
      case _             => throw new IllegalArgumentException("CheckInvoice response wasn't boolean")
    })
  }

  override def sendTo(lnInvoice: String, msat: Long): Future[String] = ???

  private def rpcCall(rpcRequest: JsonRPCRequest): Future[JsonRPCResponse] = {
    val request = makeHttpRequest(rpcRequest)
    logger.info(s"calling eclair RPC ${request.toString()}")
    Http().singleRequest(request).map(handleResponse)
  }

  private def makeHttpRequest(payload: JsonRPCRequest): HttpRequest = {
    HttpRequest()
      .withMethod(POST)
      .withUri(s"http://$eclairHost:$eclairPort")
      .addCredentials(BasicHttpCredentials("", apiPassword)) // name is not used
      .withEntity(HttpEntity(
        (write(payload)))
        .withContentType(ContentType(`application/json`))
      )
  }

  private def handleResponse(httpResponse: HttpResponse): JsonRPCResponse = {
    //FIXME remove Await and hardcoded timeouts!!
    val entityAsString = Await.result(httpResponse.entity.toStrict(5 seconds).map(_.data.decodeString("UTF-8")), 10 seconds)
    logger.info(s"Eclair response: $entityAsString")
    httpResponse.status match {
      case OK    => parseJson(entityAsString).extract[JsonRPCResponse]
      case notOk => throw new IllegalArgumentException(s"Received status $notOk while calling eclair")
    }
  }

}
