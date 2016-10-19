import com.typesafe.sbt.packager.archetypes._
import com.typesafe.sbt.packager.archetypes.systemloader.SystemdPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object ThisBuild extends Build {

  import Dependencies._
  import Resolvers._

  val buildOrganization = "paypercom"
  val buildVersion = "0.0.1"
  val buildScalaVersion = "2.11.8"

  val name = "paypercom-hotspot"
  val jarName = s"$name.jar"


  val buildSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    shellPrompt := ShellPrompt.buildShellPrompt,
    resolvers := repositories,
    libraryDependencies := dependencies
  )


  lazy val paypercomHotspot = Project(
    name, file("."),
    settings = buildSettings
   ).enablePlugins(JavaAppPackaging, SystemdPlugin, DebianPlugin, UniversalPlugin)
    .settings(parallelExecution in Compile := true)
    .settings(parallelExecution in Test := false)
    .settings(sources in (Compile, doc) := Seq.empty)
    .settings(mainClass in (Compile, assembly) := Some("Boot"))
    .settings(assemblyJarName in assembly := jarName)
    .settings(mappings in Universal <<= (mappings in Universal, assembly in Compile) map { (mappings, fatJar) =>
      val filtered = mappings filter { case (file, name) =>  ! name.endsWith(".jar") }
      filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    })


  def currentGitBranch = {
    "git rev-parse --abbrev-ref HEAD".lines_!.mkString.replaceAll("/", "-").replaceAll("heads-", "")
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
  val json4sVersion = "3.+"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion
  )

  val json4s= Seq(
    "org.json4s" %% "json4s-native" % json4sVersion,
    "org.json4s" %% "json4s-ext" % json4sVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.+"
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  )

  val  bitcoinj = Seq(
    "org.bitcoinj" % "bitcoinj-core" % "0.14.3"
  )

  val dependencies = akka ++ json4s ++ logging ++ bitcoinj
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
