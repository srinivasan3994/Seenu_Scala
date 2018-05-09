

lazy val commonSettings = Seq(
organization  := "com.sce",
version := "1.0",
scalaVersion := "2.11.8"
)


lazy val root = (project in file(".")).enablePlugins( SonarRunnerPlugin)
  .settings(
name := "scs-server",
commonSettings,
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-contrib" % "2.4.8",
  "com.google.inject" % "guice" % "4.1.0",
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.1.0",
  "net.virtual-void" %% "json-lenses" % "0.6.1",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.apache.kafka" % "kafka-clients" % "0.9.0.0",
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.github.tminglei" %% "slick-pg" % "0.6.0",
  "com.github.tminglei" %% "slick-pg_joda-time" % "0.6.0",
  "com.typesafe.slick" %% "slick-extensions" % "2.1.0",
  //"com.microsoft.sqlserver" % "mssql-jdbc" % "6.1.0.jre8" % "test",
  "net.sourceforge.jtds" % "jtds" % "1.3.1",
  //"com.github.noraui" % "ojdbc7" % "12.1.0.2",
  "com.jayway.jsonpath" % "json-path" % "2.2.0",
  "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0",
  "com.nulab-inc" %% "akka-http-oauth2-provider" % "1.3.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.10",
  "ch.qos.logback" % "logback-classic" % "1.1.7"
  
),
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8", "-language:postfixOps"),
test in assembly := {},

assemblyJarName in assembly := "scs-botv1.3Dev.jar",

mainClass in assembly := Some("com.sce.main.Main"),

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)