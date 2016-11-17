package resources


import akka.http.scaladsl.model._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.Uri._
import akka.http.scaladsl.server.Route
import commons.Configuration.MiniPortalConfig._

/**
  * Created by andrea on 19/10/16.
  */
trait StaticFiles extends CommonResource with ExtraDirectives {

  /**
    * Serves all static files in the given folder
    */
  def staticFilesRoute:Route = getFromDirectory(staticFilesDir)

  /**
    * Performs browser redirect
    */
  def preloginRoute:Route = get {
    path("prelogin"){
      complete {
        HttpEntity(
          browserRedirectPage
        ).withContentType(`text/html`.toContentType(`UTF-8`))
      }
    }
  }

  /**
    * Redirects the user to the prelogin
    */
  def entryPointRoute:Route = get {
    extractClientMAC { clientMAC =>
      extractRequest { httpRequest =>
        redirect(preLoginUrl(clientMAC, httpRequest), StatusCodes.TemporaryRedirect)
      }
    }
  }

  def preLoginUrl(clientMAC:Option[String], request: HttpRequest):Uri = {

    Uri()
      .withScheme("http")
      .withHost(miniPortalHost)
      .withPort(miniPortalPort)
      .withPath(Path("/prelogin"))
      .withQuery(Query(
        "userUrl" -> request._2.toString,
        "mac" -> clientMAC.getOrElse("unknown"),
        "sessionId" -> java.util.UUID.randomUUID.toString
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
      |
    """.stripMargin


}
