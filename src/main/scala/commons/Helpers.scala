package commons

import Configuration._
import akka.actor.ActorSystem
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import scala.concurrent._
import scala.reflect.ClassTag
import scala.reflect._

/**
  * Created by andrea on 21/10/16.
  */
package object Helpers {

  private def actorPathFor[T:ClassTag] = {
    s"${config.getString("akka.actorSystem")}/user/${classTag[T].runtimeClass.getSimpleName}$$"
  }

  def actorRefFor[T:ClassTag](implicit actorSystem:ActorSystem) = {
    actorSystem.actorSelection(s"akka://${actorPathFor[T]}")
  }

  object ScalaConversions {

    implicit class ListenableFutureToScalaFuture[T](lfuture:ListenableFuture[T]) {
      def toScalaFuture:Future[T] = {
        val promise = Promise[T]()
        Futures.addCallback(lfuture, new FutureCallback[T] {
          override def onFailure(t: Throwable): Unit = promise failure t
          override def onSuccess(result: T): Unit = promise success result
        })

        promise.future
      }
    }

  }


}
