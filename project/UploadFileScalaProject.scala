import sbt.Keys._
import sbt._

object AuctionScalaProject extends Build with BuildExtra{
  import Resolvers._
  lazy val root = Project("upload-for-large-files", file(".")) settings(coreSettings : _*)

  lazy val commonSettings: Seq[Setting[_]] = Seq(
    organization := "upload-for-large-files",
    version := "0.1",
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.10.4", "2.11.1"),
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps"),
    resolvers ++= Seq(akkaRelease, akkaSnapshot, sonatypeRelease, sonatypeSnapshot)
  )

  lazy val coreSettings = commonSettings ++ Seq(
    name := "upload-for-large-files",
    libraryDependencies :=
      Seq(
        "ch.qos.logback"      % "logback-classic"  % "1.0.13",
        "io.spray"            % "spray-can"        % "1.3.1",
        "io.spray"            % "spray-http"    % "1.3.1",
        "io.spray"            % "spray-client"    % "1.3.1",
        "com.typesafe.akka"  %% "akka-actor"       % "2.3.0",
        "com.typesafe.akka"  %% "akka-slf4j"       % "2.3.0",
        "io.spray"            % "spray-httpx"    % "1.3.1",
        "io.spray"            % "spray-util"    % "1.3.1",
        "io.spray" %%  "spray-json" % "1.2.6",
        "com.typesafe"         %   "config"            % "1.0.0",
        "com.typesafe"        %% "scalalogging-slf4j" % "1.0.1",
        "org.rabinfingerprint" % "rabinfingerprint" % "1.0.0-SNAPSHOT",
        "org.pegdown"         % "pegdown" % "1.4.2",
        "commons-io" % "commons-io" % "2.4",
        "commons-codec" % "commons-codec" % "1.9",
        "org.apache.commons"   % "commons-lang3"          % "3.1",
        "org.scalautils" % "scalautils_2.10" % "2.0",
        "io.spray" % "spray-testkit" % "1.3.1" % "test",
        "org.specs2" %% "specs2" % "2.4.2" % "test",
        "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
      ),

    parallelExecution in Test := false,

    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    publishMavenStyle := true,
    publishArtifact in Test := false

  )
}


object Resolvers {
  val akkaRelease = "typesafe release repo" at "http://repo.typesafe.com/typesafe/releases/"
  val akkaSnapshot = "typesafe snapshot repo" at "http://repo.typesafe.com/typesafe/snapshots/"
  val sonatypeSnapshot = "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  val sonatypeRelease = "Sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/"


}
