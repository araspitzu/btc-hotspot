/*
 * btc-hotspot
 * Copyright (C) 2016  Andrea Raspitzu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package commons

import java.io.{ BufferedReader, File, InputStreamReader }

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.util.Failure

package object Helpers extends LazyLogging {

  def addShutDownHook(hook: => Unit) = {
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run: Unit = hook
    })
  }

  implicit class FutureOption[+T](val future: Future[Option[T]]) {

    def flatMap[U](f: T => FutureOption[U])(implicit ec: ExecutionContext): FutureOption[U] = {
      FutureOption {
        future.flatMap { optA =>
          optA.map { a =>
            f(a).future
          } getOrElse Future.successful(None)
        }
      }
    }

    def map[U](f: T => U)(implicit ec: ExecutionContext): FutureOption[U] = FutureOption(future.map(_ map f))

    def orFailWith[U >: T](error: String)(implicit ec: ExecutionContext): Future[U] = future map {
      case Some(t) => t
      case None    => throw new NoSuchElementException(error)
    }

    //    def orElse[U >: T](other: U): FutureOption[U] = future map {
    //      case Some(t) => Some(t)
    //      case None    => Some(other)
    //    }
    //
    //    def colleziona[U](pf: PartialFunction[T, U])(implicit executor: ExecutionContext): FutureOption[U] = future.map {
    //      case Some(t) => ???
    //      case None => ???
    //    }
    //
    //    def recoverWith[U >: T](other: FutureOption[U]): FutureOption[U] = future.recoverWith {
    //      case t: Throwable =>
    //        logger.warn(s"Future failed: ${t.getMessage}", t)
    //        other.future
    //    }

  }

  implicit class CmdExecutor(cmd: String)(implicit ec: ExecutionContext) {
    def exec: Future[String] = {
      val result = Future {
        logger.info(s"Executing '$cmd'")
        val proc = Runtime.getRuntime.exec(cmd)

        val exitValue = proc.waitFor
        if (exitValue != 0)
          throw new IllegalStateException(s"\'$cmd\' exited with code $exitValue")

        val stdIn = new BufferedReader(new InputStreamReader(proc.getInputStream))
        val stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream))

        val output = stdIn.lines.iterator.asScala.fold("")(_ + _)
        val errOut = stdErr.lines.iterator.asScala.fold("")(_ + _)

        if (!errOut.isEmpty) {
          logger.error(errOut)
        }

        logger.debug("Output: "+output)
        output
      }
      result.onComplete {
        case Failure(err: Throwable) =>
          logger.error(s"Error executing cmd [$cmd]!", err)
        case other => other
      }

      result
    }

  }

}
