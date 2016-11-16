package resources


import akka.http.scaladsl.model._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.Uri._
import akka.http.scaladsl.server.Route
import commons.Configuration._
import scala.compat.java8.OptionConverters._
import scala.io.Source

/**
  * Created by andrea on 19/10/16.
  */
trait StaticFiles extends CommonResource {

  val staticFilesDir = config.getString("miniportal.staticFilesDir")
  val miniPortalHost = config.getString("miniportal.host")
  val miniPortalPort = config.getInt("miniportal.port")
  val miniPortalIndex = config.getString("miniportal.index")

  def staticFilesRoute:Route = getFromDirectory(staticFilesDir)

  def preloginRoute:Route = get {
    path("prelogin"){
      complete {
        HttpEntity(
          browserRedirectPage
        ).withContentType(`text/html`.toContentType(`UTF-8`))
      }
    }
  }

  def entryPointRoute:Route = get {
    extractClientIP { srcIp =>
      extractRequest { httpRequest =>
        redirect(miniPortalUrl(srcIp, httpRequest), StatusCodes.TemporaryRedirect)
      }
    }
  }

  def miniPortalUrl(clientIp:RemoteAddress, request: HttpRequest):Uri = {

    val mac = for {
      ipAddr <- clientIp.getAddress.asScala.map(_.getHostAddress)
      macAddr <- arpLookup(ipAddr)
    } yield macAddr

    Uri()
      .withScheme("http")
      .withHost(miniPortalHost)
      .withPort(miniPortalPort)
      .withPath(Path("/prelogin"))
      .withQuery(Query(
        "userUrl" -> request._2.toString,
        "mac" -> mac.getOrElse("unknown"),
        "sessionId" -> java.util.UUID.randomUUID.toString
      ))
  }

  def arpLookup(ipAddr:String) = {
    Source
      .fromFile("/proc/net/arp")
      .getLines
      .find(_.startsWith(ipAddr)).map(_.substring(41,58))
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
      |  setTimeout(redirect, 2500);
      |}
      |</script>
      |<meta http-equiv="refresh" content="7; URL=/prelogin">
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
