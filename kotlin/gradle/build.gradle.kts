plugins {
  idea
  kotlin("jvm")               version "2.4.20"
  kotlin("plugin.allopen")    version "2.4.20"

  id("com.diffplug.spotless") version "8.10.3"
  id("io.gatling.gradle")     version "3.16.0"
}

tasks.withType(JavaCompile::class) {
  options.release.set(21)
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
  }
}

repositories {
  mavenCentral()
  // Gatling's private repository of public milestones: the plugin and the Gatling version it is built against are milestones,
  // which are not on Maven Central. Export CODEARTIFACT_AUTH_TOKEN first, see the README
  maven {
    url = uri("https://gatlingcorp-049894548288.d.codeartifact.us-east-1.amazonaws.com/maven/staging")
    credentials {
      username = "aws"
      password = providers.environmentVariable("CODEARTIFACT_AUTH_TOKEN").orNull
    }
  }
}

dependencies {
  gatlingImplementation("io.gatling:gatling-kafka-java:3.16.1-M2")
  // Gatling itself at the milestone the plugin is built against. The Gradle plugin adds the HTML reports' charts at its own
  // version, 3.16.0: there is no milestone of them, and that release works with this milestone
  gatlingImplementation("io.gatling:gatling-app:3.16.1-M2")
}

gatling {
  // Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://docs.gatling.io/reference/integrations/build-tools/gradle-plugin/#running-your-simulations-on-gatling-enterprise-cloud
}

spotless {
  kotlin {
    ktfmt()
      .googleStyle()
  }
}
