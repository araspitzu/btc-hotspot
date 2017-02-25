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

package resources

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.server.Route
import commons.AppExecutionContextRegistry.context._
import commons.Configuration.MiniPortalConfig.{miniPortalHost, miniPortalPort}
import services.{SessionServiceImpl, SessionServiceRegistry}

/**
  * Created by andrea on 19/10/16.
  */
trait CaptiveResource extends CommonResource with ExtraDirectives {


  /**
    * Redirects the user to prelogin
    */
  def entryPointRoute:Route = get {
    extractRequest { httpRequest =>
      redirectToPrelogin(Some(httpRequest))
    }
  }

  /**
    * Create the session and performs browser redirect
    */
  def preloginRoute:Route = get {
    path("prelogin") {
      extractClientMAC { clientMac =>
        complete {
          SessionServiceRegistry.sessionService.getOrCreate(clientMac.getOrElse("unknown")).map { _ =>
            HttpEntity(
              browserRedirectPage
            ).withContentType(`text/html`.toContentType(`UTF-8`))
          }
        }
      }
    }
  }
  
  def redirectToPrelogin(request: Option[HttpRequest] = None) =
    redirect(preLoginUrl(request), StatusCodes.TemporaryRedirect)
  
  private def preLoginUrl(request: Option[HttpRequest]):Uri = {
    Uri()
      .withScheme("http")
      .withHost(miniPortalHost)
      .withPort(miniPortalPort)
      .withPath(Path("/prelogin"))
      .withQuery(Query(
        "userUrl" -> request.map(_._2.toString).getOrElse("duckduckgo.com")
      ))
  }
  
  
  val browserRedirectPage =
    """
      |<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
      |<html><head><title>redirect...</title>
      |
      |<script type="text/javascript" language="Javascript">
      |var loginUrl;
      |function redirect() { window.location = loginUrl; return false; }
      |window.onload = function() {
      |  var href = window.location.href;
      |  loginUrl = "index.html"+href.substring(href.indexOf("?"), href.length);
      |  setTimeout(redirect, 1500);
      |}
      |</script>
      |</head>
      |<body style="margin: 0pt auto; height:100%;">
      |<div style="width:100%;height:80%;position:fixed;display:table;">
      |<p style="display: table-cell; line-height: 2.5em;
      |vertical-align:middle;text-align:center;color:black;">
      |<a href="#" onclick="javascript:return redirect();">
      |<img src="img/press-02.jpg" alt="" border="0" height="39" width="123"/></a><br>
      |<small><img src="img/hamster.gif"/> <br> redirecting...</small></p>
      |<br><br>
      |</div>
      |</body>
      |</html>
    """.stripMargin


}
