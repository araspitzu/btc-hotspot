package ln

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
import commons.Configuration.EclairConfig._
import commons.JsonSupport
import ln.model._
import registry.Registry
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object EclairClientRegistry extends Registry with EclairClientComponent {

  override val eclairClient: EclairClient = new EclairClientImpl

}

trait EclairClientComponent {

  val eclairClient: EclairClient

}

trait EclairClient {

  def connect(uri: String): Future[String]

  def openChannel(msat: Long, peer: String): Future[String]

  def getInvoice(msat: Long, msg: String): Future[String]

  def checkInvoice(invoice: String): Future[Boolean]

  def payInvoice(lnInvoice: String): Future[EclairSendResponse]

  def isReady(): Future[Boolean]

}

class EclairClientImpl extends EclairClient with JsonSupport with LazyLogging {

  override def isReady(): Future[Boolean] = ???

  override def connect(uri: String): Future[String] = {
    rpcCall[String](method = "connect", uri)
  }

  override def openChannel(msat: Long, peer: String): Future[String] = {
    rpcCall[String]("open", peer, msat, 1) //1 for push_msat parameter
  }

  def getInfo(): Future[EclairGetInfoResponse] = {
    rpcCall[EclairGetInfoResponse](method = "getinfo")
  }

  override def getInvoice(msat: Long, msg: String): Future[String] = {
    rpcCall[String]("receive", msat, msg)
  }

  override def checkInvoice(invoiceHash: String): Future[Boolean] = {
    rpcCall[Boolean]("checkpayment", invoiceHash)
  }

  override def payInvoice(lnInvoice: String): Future[EclairSendResponse] = {
    rpcCall[EclairSendResponse]("send", lnInvoice)
  }

  private def rpcCall[T](method: String, params: Any*)(implicit mf: scala.reflect.Manifest[T]): Future[T] = {
    val request = createHttpRequest(JsonRPCRequest(method = method, params = params))
    logger.info(s"calling eclair RPC '$method'")
    Http().singleRequest(request).map(handleResponse).map {
      case JsonRPCResponse(result, None, _)   => result.extract[T]
      case JsonRPCResponse(_, Some(error), _) => throw new IllegalStateException(error.message)
    }
  }

  private def createHttpRequest(payload: JsonRPCRequest): HttpRequest = {
    HttpRequest()
      .withMethod(POST)
      .withUri(s"$protocol://$host:$port")
      .addCredentials(BasicHttpCredentials("", apiPassword)) // name is not used
      .withEntity(HttpEntity((write(payload)))
        .withContentType(ContentType(`application/json`)))
  }

  private def handleResponse(httpResponse: HttpResponse): JsonRPCResponse = {
    //FIXME remove Await and hardcoded timeouts!!
    val entityAsString = Await.result(httpResponse.entity.toStrict(30 seconds).map(_.data.decodeString("UTF-8")), 30 seconds)
    logger.debug(s"Eclair response: $entityAsString")
    httpResponse.status match {
      case OK    => parseJson(entityAsString).extract[JsonRPCResponse]
      case notOk => throw new IllegalArgumentException(s"Received status $notOk while calling eclair")
    }
  }

}
