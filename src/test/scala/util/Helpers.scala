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

package util

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

import commons.Helpers.FutureOption

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * Created by andrea on 09/12/16.
 */
object Helpers {

  implicit class FutureWait[T](f: Future[T]) {

    def futureValue: T = Await.result(f, 30 seconds)

  }

  implicit class FutureOptionWait[T](f: FutureOption[T]) {

    def futureValue: Option[T] = f.future.futureValue

  }

  val futureNone = FutureOption(Future.successful(None))

  def futureSome[T]: T => FutureOption[T] = { t =>
    FutureOption(Future.successful(Some(t)))
  }

}
