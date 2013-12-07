import AssemblyKeys._

assemblySettings

jarName in assembly := "downloader.jar"

mainClass in assembly := Some("com.example.Boot")

test in assembly := {}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList(ps @ _*) if ps.last endsWith "pom.xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "pom.properties" => MergeStrategy.first
  case x => old(x)
}
}

net.virtualvoid.sbt.graph.Plugin.graphSettings

name          := "downloader"

organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.10.2"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-Yno-adapted-args")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

Revolver.reStartArgs := Seq("-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=4242 -Dcom.sun.management.jmxremote.local.only=false")

libraryDependencies ++= {
  val akkaV = "2.2.0"
  val sprayV = "1.2.0"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-client"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % "1.2.5" intransitive,
    "io.spray"            %   "spray-testkit" % sprayV,
    "com.typesafe.akka"   %   "akka-actor_2.10"    % akkaV,
    "com.typesafe.akka"   %   "akka-testkit_2.10"  % akkaV,
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test",
    "net.liftweb"         %%  "lift-json"      % "2.6-M1"
  )
}

seq(Revolver.settings: _*)