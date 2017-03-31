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

import com.typesafe.sbt.packager.archetypes._
import com.typesafe.sbt.packager.archetypes.systemloader.SystemdPlugin
import com.typesafe.sbt.packager.debian.{PackageInfo, DebianPlugin}
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport._
import com.typesafe.sbt.packager.linux.{LinuxSymlink, LinuxPackageMapping}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport._
import com.typesafe.sbt.packager.MappingsHelper._
import sbt.Keys._
import sbt._

object ThisBuild extends Build {

  import Dependencies._
  import Resolvers._
  import PackageSetting._
  import DebianSetting._
  

  val buildOrganization = "andrea"
  val buildVersion = "0.0.1"
  val buildScalaVersion = "2.12.1"
  val name = "btc-hotspot"


  val buildSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    fork in Test := true,
    shellPrompt := ShellPrompt.buildShellPrompt,
    resolvers := repositories,
    libraryDependencies := dependencies,
    parallelExecution in Compile := true,
    sources in (Compile, doc) := Seq.empty,
    mainClass in Compile := Some("Boot")
  )

  lazy val paypercomHotspot = Project(
    name, file("."),
    settings = buildSettings ++ universalPluginSettings ++ debianSettings
   ).enablePlugins(JavaServerAppPackaging, SystemdPlugin, DebianPlugin, UniversalPlugin)

}

object DebianSetting {

  def debianSettings = Seq(
    debianPackageInfo in Debian := PackageInfo(
      name = ThisBuild.name,
      version = ThisBuild.buildVersion,
      maintainer = "Andrea Raspitzu",
      summary = "Hotspot enabled app",
      description = "Enable your home connection to be sold for bitcoin"
    )
  )

}

object PackageSetting {

  //TODO find how to retrieve the target directory absolute path
  private val targetDirectory = s"/usr/share/${ThisBuild.name}"

  def universalPluginSettings = Seq(
    javaOptions in Universal ++= Seq(
      // -J params will be added as jvm parameters
      "-J-Xmx640m",
      "-J-Xms350m",
      s"-Dconfig.file=${targetDirectory}/${confFileMapping.value._2}",
      s"-Dlogback.configurationFile=${targetDirectory}/${logbackConfMapping.value._2}"
    ),
    mappings in Universal ++= {
      staticDirMapping.value ++ Seq(
        confFileMapping.value,
        logbackConfMapping.value
      )
    },
    walletDirectory,
    walletDirSymlink
  )


  private def logbackConfMapping = Def.setting {
    val logback = (resourceDirectory in Compile).value / "logback.xml"
    logback -> "conf/logback.xml"
  }

  private def confFileMapping = Def.setting {
    val conf = (resourceDirectory in Compile).value / "application.conf"
    conf -> "conf/application.conf"
  }

  /*
    Copies directory "static" and its content into target directory /usr/share/<app>/
   */
  private def staticDirMapping = Def.setting {
    directory(baseDirectory.value / "static")
  }

  private def walletDirectory = {
    linuxPackageMappings += packageTemplateMapping(
      s"/opt/${ThisBuild.name}/bitcoin"
    )().withUser((daemonUser in Linux).value)
      .withGroup((daemonGroup in Linux).value)
      .withPerms("755")
  }

  private def walletDirSymlink = {
    linuxPackageSymlinks += {
      LinuxSymlink(s"$targetDirectory/bitcoin", s"/opt/${ThisBuild.name}/bitcoin")
    }
  }

}

object Resolvers {
  val typesafe = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

  //Mantainer of akka-http-json
  val hseeberger = Resolver.bintrayRepo("hseeberger", "maven")

  val repositories = Seq(typesafe, hseeberger)
}


object Dependencies {
  val akkaVersion = "2.4.+"
  val akkaHttpVersion = "10.+"
  val json4sVersion = "3.+"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
  )

  val json4s= Seq(
    "org.json4s" %% "json4s-native" % json4sVersion,
    "org.json4s" %% "json4s-ext" % json4sVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.+"
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.+"
  )

  val  bitcoinj = Seq(
    "org.bitcoinj" % "bitcoinj-core" % "0.14.+"
  )

  val java8converters = Seq(
    "org.scala-lang.modules" % "scala-java8-compat" % "0.8.0"
  )

  val db = Seq(
    "com.typesafe.slick" %% "slick" % "3.2.+",
    "com.h2database" % "h2" % "1.4.+",
    "com.zaxxer" % "HikariCP" % "2.5.+" % "test"
  )
  
  val testing = Seq(
    "org.specs2" %% "specs2-core" % "3.8.6" % "test",
    "org.specs2" %% "specs2-mock" % "3.8.6" % "test",
    "org.specs2" %% "specs2-matcher-extra" % "3.8.6" % "test"
  )
  
  val misc = Seq(
    "org.apache.commons" % "commons-email" % "1.4"
  )
  

  val dependencies = akka ++ json4s ++ logging ++ bitcoinj ++ db ++ testing ++ misc
}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
    )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract(state).currentProject.id
      "%s:%s:%s> ".format(
        currProject, currBranch, ThisBuild.buildVersion
      )
    }
  }
}
