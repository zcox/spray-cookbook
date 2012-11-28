organization := "com.pongr"

name := "spray-cookbook"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Spray" at "http://repo.spray.io/",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "io.spray"          %  "spray-can"       % Version.spray,
  "io.spray"          %  "spray-routing"   % Version.spray,
  "io.spray"          %  "spray-testkit"   % Version.spray % "test",
  "com.typesafe.akka" %  "akka-actor"      % Version.akka,
  "com.typesafe.akka" %  "akka-slf4j"      % Version.akka,
  "org.clapper"       %% "grizzled-slf4j"  % "0.6.9",
  "ch.qos.logback"    %  "logback-classic" % "1.0.6",
  "org.specs2"        %% "specs2"          % "1.12.2" % "test" //same version as spray-testkit
)

scalacOptions += "-Ydependent-method-types" //http://spray.io/documentation/spray-routing/installation/
