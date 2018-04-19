import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
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

val env = sys.props.getOrElse("env", default = "local")
val buildVersion = sys.props.getOrElse("version", default = "0.0.1")
val buildName = "btc-hotspot"

val akkaVersion = "2.4.20"
val akkaHttpVersion = "10.+"
val json4sVersion = "3.+"

lazy val btc_hotspot = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "araspitzu",
      scalaVersion    := "2.12.4",
      version         := buildVersion,
      mainClass in Compile := Some("Boot"),
      javaOptions in Test += "-Dconfig.file=src/test/resources/application.conf",
      fork in Test := true,
      ScalariformKeys.preferences := scalariformPref.value
    )),
    name := buildName,
    debianPackageInfo in Debian := debianSettings,
    libraryDependencies ++= Seq(
      //AKKA
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",

      //JSON4S
      "org.json4s" %% "json4s-native" % json4sVersion,
      "org.json4s" %% "json4s-ext" % json4sVersion,
      "de.heikoseeberger" %% "akka-http-json4s" % "1.+",

      //LOGGING
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.+",

      //DB
      "com.typesafe.slick" %% "slick" % "3.2.+",
      "com.h2database" % "h2" % "1.4.+",
      "com.zaxxer" % "HikariCP" % "2.5.+" % "test",

      //TEST FRAMEWORK
      "org.specs2" %% "specs2-core" % "3.8.6" % "test",
      "org.specs2" %% "specs2-mock" % "3.8.6" % "test",
      "org.specs2" %% "specs2-matcher-extra" % "3.8.6" % "test",

      //MISC
      "org.apache.commons" % "commons-email" % "1.4"
    )
  ).settings(universalPluginSettings)

enablePlugins(JavaServerAppPackaging, SystemdPlugin, DebianPlugin, UniversalPlugin, JDebPackaging)


/**
  *  native-packager plugin configuration
  */

//DEBIAN PACKAGE SETTINGS
lazy val debianSettings = PackageInfo(
        name = buildName,
        version = buildVersion,
        maintainer = "Andrea Raspitzu",
        summary = "Hotspot enabled app",
        description = "Enable your home connection to be sold for bitcoin"
)

//UNIVERSAL

lazy val targetDirectory = s"/usr/share/$buildName"

/**
    Copies directory "static" and its content into target directory /usr/share/<app>/
 */
lazy val staticDirMapping = Def.setting {
    directory(baseDirectory.value / "static")
}

lazy val universalPluginSettings = Seq(
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


lazy val logbackConfMapping = Def.setting {
  val logback = env match {
    case "hotspot" => (resourceDirectory in Compile).value / "hotspot_logback.xml"
    case _ => (resourceDirectory in Compile).value / "logback.xml"
  }
  logback -> "conf/logback.xml"
}

lazy val confFileMapping = Def.setting {
  val conf = env match {
    case "hotspot" => (resourceDirectory in Compile).value / "hotspot.conf"
    case _ => (resourceDirectory in Compile).value / "application.conf"
  }
  conf -> "conf/application.conf"
}

/**
  *
  */
lazy val walletDirectory = {
  linuxPackageMappings += packageTemplateMapping(
    s"/opt/$buildName/bitcoin"
  )().withUser((daemonUser in Linux).value)
    .withGroup((daemonGroup in Linux).value)
    .withPerms("755")
}


lazy val walletDirSymlink = linuxPackageSymlinks += {
    LinuxSymlink(s"$targetDirectory/bitcoin", s"/opt/$buildName/bitcoin")
}


lazy val scalariformPref = Def.setting {
  ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(CompactStringConcatenation, true)
}
