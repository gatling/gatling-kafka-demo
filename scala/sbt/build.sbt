val gatlingVersion = "3.16.0"
val gatlingKafkaVersion = "3.16.1.FL-M1"
// this build of the plugin is the Gatling Enterprise one: it runs on the Gatling Enterprise build of Gatling's core
val gatlingEnterpriseVersion = "3.16.0.FL"

// gatling-kafka is resolved from Gatling's private repositories, which a global sbt file declares with their credentials (see the README)
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
      "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test"),
    dependencyOverrides ++= Seq(
      "io.gatling"            % "gatling-core"              % gatlingEnterpriseVersion,
      "io.gatling"            % "gatling-core-java"         % gatlingEnterpriseVersion)
  )
