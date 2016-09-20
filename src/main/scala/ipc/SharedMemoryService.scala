package ipc

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
  * Created by andrea on 15/09/16.
  */
class SharedMemoryService extends Actor with LazyLogging {

  private var sharedMemory:Option[SharedMemory] = None

  override def receive: Receive = {
    case entry:SharedStruct => {
      logger.info("Yo adding a new entry yo")
      sharedMemory.map(_.addEntry(entry))
    }
    case READ_MEM => {
      logger.info("Yo READ_MEM here!")
      sender() ! (sharedMemory.map(_.printEntries))
    }
    case other => logger.warn(s"Got $other")
  }

  override def preStart():Unit = {
    sharedMemory = Some(SharedMemory())
  }

  override def preRestart(reason : Throwable, message : Option[Any]){
    logger.warn(s"Restarting SharedMemoryService because $message", reason)
    preStart
  }
}

sealed trait ActorMsg
case object READ_MEM extends ActorMsg
