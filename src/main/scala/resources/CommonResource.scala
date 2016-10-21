package resources

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{PredefinedToRequestMarshallers, PredefinedToResponseMarshallers, PredefinedToEntityMarshallers, GenericMarshallers}
import akka.http.scaladsl.model.HttpHeader
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

  val bitcoinPaymentRequest:HttpHeader = {
    HttpHeader.parse("Accept","application/bitcoin-paymentrequest") match {
      case Ok( header, errors ) => header
      case _ => throw new RuntimeException("Unable to parse payment request header")
    }
  }

}