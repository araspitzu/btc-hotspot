package resources

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{PredefinedToRequestMarshallers, PredefinedToResponseMarshallers, PredefinedToEntityMarshallers, GenericMarshallers}
import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext

/**
  * Created by andrea on 09/09/16.
  */
trait CommonResource extends Directives with Json4sSupport {

  implicit def actorSystem: ActorSystem

  implicit val executionContext:ExecutionContext
}

object CommonMarshallers extends GenericMarshallers
  with PredefinedToEntityMarshallers
  with PredefinedToResponseMarshallers
  with PredefinedToRequestMarshallers  { }