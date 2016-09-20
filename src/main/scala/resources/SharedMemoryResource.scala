package resources

import akka.http.scaladsl.marshalling.GenericMarshallers
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.util.Timeout
import ipc.{READ_MEM, SharedStruct}
import scala.concurrent.{ExecutionContext}
import scala.concurrent.duration._

/**
  * Created by andrea on 15/09/16.
  */
trait SharedMemoryResource extends CommonResource with GenericMarshallers {

  lazy val sharedMemoryServiceActor = actorSystem.actorSelection("user/sharedMemoryActor")

  implicit val timeout = Timeout(10 seconds)

  def route: Route = {
    post {
      path("shmem" / "add"){
        complete {
          sharedMemoryServiceActor ! SharedStruct.aStruct

          "Yo\n"
        }

      }
    } ~ get {
      path("shmem" / "list"){
        complete{
          sharedMemoryServiceActor.ask(READ_MEM).map(_.asInstanceOf[String])

          "Yo\n"
        }
      }
    }
  }

}
