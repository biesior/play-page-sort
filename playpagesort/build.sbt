name := """playpagesort"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.37",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.play" %% "play-mailer" % "4.0.0-M1",
  "com.notnoop.apns" % "apns" % "1.0.0.Beta6",
  "com.ganyo" % "gcm-server" % "1.0.2",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.tika" % "tika-core" % "1.11",
  "org.apache.commons" % "commons-lang3" % "3.4"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
