package resources

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{PredefinedToRequestMarshallers, PredefinedToResponseMarshallers, PredefinedToEntityMarshallers, GenericMarshallers}
import akka.http.scaladsl.model.{ContentType, HttpHeader}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.server.Directives
import com.typesafe.scalalogging.slf4j.LazyLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext

/**
  * Created by andrea on 09/09/16.
  */
trait CommonResource extends Directives with Json4sSupport with LazyLogging {

  implicit def actorSystem: ActorSystem

  implicit val executionContext:ExecutionContext
}

object CommonMarshallers extends GenericMarshallers
  with PredefinedToEntityMarshallers
  with PredefinedToResponseMarshallers
  with PredefinedToRequestMarshallers  { }

object ExtraHttpHeaders {

  private val paymentRequestType = "application/bitcoin-paymentrequest"

  val paymentRequestContentType:ContentType = ContentType.parse(paymentRequestType) match {
    case Right(contentType) => contentType
    case Left(err) => throw new RuntimeException(s"error occurred: ${err.toString}")
  }

  val contentTypePaymentRequest = {
    HttpHeader.parse("Content-Type",paymentRequestType) match {
      case Ok(header, errors) => header
      case _ => throw new RuntimeException("Unable to parse content type payment request header")
    }
  }

  val acceptBitcoinPaymentRequest:HttpHeader = {
    HttpHeader.parse("Accept",paymentRequestType) match {
      case Ok( header, errors ) => header
      case _ => throw new RuntimeException("Unable to parse payment request header")
    }
  }

}