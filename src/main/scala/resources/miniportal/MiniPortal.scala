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

package resources.miniportal

import akka.http.scaladsl.server.Route
import commons.Configuration.MiniPortalConfig._
import resources.CaptiveResource

trait MiniPortal extends CaptiveResource with OffersAPI with SessionAPI with InvoiceAPI {

  /**
   * Serves all static files in the given folder
   */
  def staticFilesRoute: Route = getFromDirectory(miniportalStaticFilesDir)

  val miniportalRoute: Route =
    staticFilesRoute ~
      invoiceRoute ~
      offersRoute ~
      statusRoute ~
      preloginRoute ~
      entryPointRoute //must stay in the last position because it matches any request

}

