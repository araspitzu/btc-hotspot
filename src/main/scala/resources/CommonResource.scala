package resources

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{PredefinedToRequestMarshallers, PredefinedToResponseMarshallers, PredefinedToEntityMarshallers, GenericMarshallers}
import akka.http.scaladsl.model.{ContentType, HttpHeader}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.LazyLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext

/**
  * Created by andrea on 09/09/16.
  */
trait CommonResource extends Directives with Json4sSupport with LazyLogging {

  implicit val actorSystem: ActorSystem

  implicit val executionContext:ExecutionContext

  implicit val materializer:ActorMaterializer
}

object ExtraHttpHeaders {

  val paymentRequestContentType: ContentType = contentTypeFor("application/bitcoin-paymentrequest")
  val paymentAckContentType: ContentType = contentTypeFor("application/bitcoin-paymentack")

  private def contentTypeFor(customContentType:String) = ContentType.parse(customContentType) match {
    case Right(contentType) => contentType
    case Left(err) => throw new RuntimeException(s"Unable to generate Content-Type for $customContentType, ${err.toString}")
  }

}