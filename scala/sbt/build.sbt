val gatlingVersion = "3.16.1"
val gatlingKafkaVersion = "3.16.1.2"

lazy val gatlingSbtPluginDemo = rootProject
  .enablePlugins(GatlingPlugin)
  .settings(
    name := "gatling-kafka-demo-sbt-scala",
    scalaVersion := "2.13.18",
    scalacOptions := Seq(
      "-encoding", "UTF-8", "-release:8", "-deprecation",
      "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps"),
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test",
      "io.gatling"            % "gatling-kafka"             % gatlingKafkaVersion % "test",
      "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test")
  )
