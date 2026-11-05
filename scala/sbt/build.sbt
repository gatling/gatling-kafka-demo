val gatlingVersion = "3.16.1-M2"
// the HTML reports' charts: there is no milestone of them, the latest release works with this milestone
val gatlingHighchartsVersion = "3.16.0"
val gatlingKafkaVersion = "3.16.1-M2"

// gatling-kafka and the Gatling version it is built against are milestones, which are not on Maven Central: they are resolved
// from Gatling's private repository of public milestones (staging), which a global sbt file declares with its credentials (see the README)
lazy val gatlingSbtPluginDemo = rootProject
  .enablePlugins(GatlingPlugin)
  .settings(
    name := "gatling-kafka-demo-sbt-scala",
    scalaVersion := "2.13.18",
    scalacOptions := Seq(
      "-encoding", "UTF-8", "-release:8", "-deprecation",
      "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps"),
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingHighchartsVersion % "test",
      "io.gatling"            % "gatling-kafka"             % gatlingKafkaVersion % "test",
      "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test")
  )
