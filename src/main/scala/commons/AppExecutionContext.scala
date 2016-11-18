package commons

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import commons.Configuration._

/**
  * Created by andrea on 18/11/16.
  */
trait AppExecutionContext {

  val context:ExecCont

  class ExecCont {
    implicit val actorSystem = ActorSystem(config.getString("akka.actorSystem"))
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatcher
  }
}

object AppExecutionContextRegistry extends AppExecutionContext {
  val context = new ExecCont
}
