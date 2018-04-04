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
import org.json4s.JsonAST.{ JBool, JString }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

trait EclairClient {

  def connect(uri: String): Future[String]

  def openChannel(msat: Long, peer: String): Future[String]

  def getInvoice(msat: Long, msg: String): Future[String]

  def checkInvoice(invoice: String): Future[Boolean]

  def sendTo(lnInvoice: String, msat: Long): Future[String]

  def isReady(): Future[Boolean]

}

class EclairClientImpl extends EclairClient with JsonSupport with LazyLogging {

  override def isReady(): Future[Boolean] = ???
  //  override def isReady(): Future[Boolean] = {
  //    for {
  //      getInfo <- rpcCall(JsonRPCRequest(method = "getinfo")).map(_.result.as[EclairGetInfoResponse])
  //      channels <- rpcCall(JsonRPCRequest(method = "getinfo")).map(_.result.as[EclairGetInfoResponse])
  //    } yield {
  //      getInfo.blockHeight > 1200 &&
  //        getInfo.alias.nonEmpty
  //    }
  //
  //  }

  //  def peers(): Future[Seq[EclairPeerResponse]] = {
  //    rpcCall(JsonRPCRequest(method = "peers")).map(_.result.as[Seq[EclairPeerResponse]])
  //  }
  //
  //  def channels(): Future[Seq[EclairChannelResponse]] = {
  //
  //    peers().map(_.filter(_.state == "CONNECTED")).map { peerList =>
  //      peerList.map { peer =>
  //        rpcCall(JsonRPCRequest(method = "channels", params = Seq(peer.nodeId))).map(_.result.as[Seq[EclairChannelResponse]])
  //      }
  //    }
  //
  //  }

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
      method = "checkpayment",
      params = invoiceHash :: Nil
    )).map(_.result match {
      case JBool(isPaid) => isPaid
      case _             => throw new IllegalArgumentException("CheckInvoice response wasn't boolean")
    })
  }

  override def sendTo(lnInvoice: String, msat: Long): Future[String] = ???

  private def rpcCall(rpcRequest: JsonRPCRequest): Future[JsonRPCResponse] = {
    val request = createHttpRequest(rpcRequest)
    logger.info(s"calling eclair RPC '${rpcRequest.method}'")
    Http().singleRequest(request).map(handleResponse)
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
    val entityAsString = Await.result(httpResponse.entity.toStrict(5 seconds).map(_.data.decodeString("UTF-8")), 10 seconds)
    logger.debug(s"Eclair response: $entityAsString")
    httpResponse.status match {
      case OK    => parseJson(entityAsString).extract[JsonRPCResponse]
      case notOk => throw new IllegalArgumentException(s"Received status $notOk while calling eclair")
    }
  }

}
