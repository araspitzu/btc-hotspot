import com.typesafe.sbt.SbtStartScript
import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._
import spray.revolver.RevolverPlugin._

object BuildSettings {
  val buildOrganization = "paypercom"
  val buildVersion = "0.0.1"
  val buildScalaVersion = "2.11.8"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    javaOptions += "-Xmx1G",
    shellPrompt := ShellPrompt.buildShellPrompt
  )
}

object Resolvers {
  val typesafe = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
  val repositories = Seq(typesafe)
}


object Dependencies {
  val akkaVersion = "2.4.10"
  val json4sVersion = "3.3.0"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion
  )

  val json4s= Seq(
    "org.json4s" %% "json4s-native" % json4sVersion,
    "org.json4s" %% "json4s-ext" % json4sVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.4.2"
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  )

  val dependencies = akka ++ json4s ++ logging
}

/**
 * Revolver.settings: (https://github.com/spray/sbt-revolver) Allows for hot reloading when JRebel is configured.
 * Integration tests should end with 'IT'. Run it:test to run integration tests only.
 * Unit tests must end with 'Spec' or 'Test'
 */
object ThisBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import Resolvers._

  val name = "traffic-authenticator"
  lazy val trafficAuthenticator = Project(
    name, file("."),
    settings = buildSettings
      ++ Seq(resolvers := repositories, libraryDependencies ++= dependencies)
      ++ SbtStartScript.startScriptForClassesSettings
      ++ Revolver.settings
      ++ assemblySettings
      ++ Seq(jarName := name + "-" + currentGitBranch + ".jar")
  ).configs(IntegrationTest)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .settings(Defaults.itSettings: _*)
    .settings(parallelExecution in Compile := true)
    .settings(parallelExecution in Test := false)
    .settings(scalaSource in IntegrationTest <<= baseDirectory / "src/test/scala")
    .settings(resourceDirectory in IntegrationTest <<= baseDirectory / "src/test/resources")
    .settings(sources in (Compile, doc) := Seq.empty)


  def currentGitBranch = {
    "git rev-parse --abbrev-ref HEAD".lines_!.mkString.replaceAll("/", "-").replaceAll("heads-", "")
  }

  def itFilter(name: String): Boolean = name endsWith "IT"

  def unitFilter(name: String): Boolean = !itFilter(name)
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
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}
