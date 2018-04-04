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
import commons.AppExecutionContextRegistry.context._

package object Helpers {

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

    def orFailWith[U >: T](error: String): Future[U] = future map {
      case Some(t) => t
      case None    => throw new NoSuchElementException(error)
    }

  }

  implicit class CmdExecutor(cmd: String) extends LazyLogging {
    def exec: Future[String] = Future {
      logger.debug(s"Executing $cmd")
      val proc = Runtime.getRuntime.exec(cmd)

      val exitValue = proc.waitFor
      if (exitValue != 0)
        throw new IllegalStateException(s"\'$cmd\' exited with code $exitValue")

      val reader = new BufferedReader(
        new InputStreamReader(proc.getInputStream)
      )

      val output = reader.lines.iterator.asScala.fold("")(_ + _)
      logger.info(output)
      output
    }

  }

}
