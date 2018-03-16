import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._



val env = sys.props.getOrElse("env", default = "local")
val buildVersion = sys.props.getOrElse("version", default = "dev")

val akkaVersion = "2.4.+"
val akkaHttpVersion = "10.+"
val json4sVersion = "3.+"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "araspitzu",
      scalaVersion    := "2.12.1",
      version         := buildVersion,
      mainClass in Compile := Some("Boot"),
      ScalariformKeys.preferences := scalariformPref.value
    )),
    name := "btc-hotspot",
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
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.+",

      //BITCOINJ
      "org.bitcoinj" % "bitcoinj-core" % "0.14.+",

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
  )

enablePlugins(JavaServerAppPackaging, SystemdPlugin, DebianPlugin, UniversalPlugin)


/**
  *  native-packager plugin configuration
  */
lazy val staticDirMapping = Def.setting {
      directory(baseDirectory.value / "static")
}


lazy val scalariformPref = Def.setting {
  ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(CompactStringConcatenation, true)
}
