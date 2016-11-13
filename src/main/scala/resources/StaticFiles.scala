package resources

import akka.http.scaladsl.model.StatusCodes.{Redirection, Success}
import akka.http.scaladsl.model.{RemoteAddress, HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.model.Uri._
import akka.http.scaladsl.server.{PathMatchers, PathMatcher, Route}
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
      .withPath(Path(miniPortalIndex))
      .withQuery(Query(
        "userUrl" -> request._2.toString,
        "mac" -> mac.getOrElse("unknown")
      ))
  }

  def arpLookup(ipAddr:String) = {
    Source
      .fromFile("/proc/net/arp")
      .getLines
      .find(_.startsWith(ipAddr)).map(_.substring(41,58))
  }


  def makeUserUrl(req:HttpRequest):String = {
    req._2.toString
  }


}
