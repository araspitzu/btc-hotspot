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

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{native, Formats, DefaultFormats}

/**
  * Created by andrea on 15/11/16.
  */
trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization

  //Append custom formats here
  implicit def json4sFormats: Formats = DefaultFormats

}
