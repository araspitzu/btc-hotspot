package commons

import Configuration._
import akka.actor.ActorSystem

import scala.reflect.ClassTag
import scala.reflect._


/**
  * Created by andrea on 21/10/16.
  */
object Helpers {

  def actorPathFor[T:ClassTag] = {
    s"${config.getString("akka.actorSystem")}/user/${classTag[T].runtimeClass.getSimpleName}$$"
  }

  def actorRefFor[T:ClassTag](implicit actorSystem:ActorSystem) = {
    actorSystem.actorSelection(s"akka://${actorPathFor[T]}")
  }

}
