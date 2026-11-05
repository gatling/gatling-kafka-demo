# Gatling Kafka - Java with Gradle demo project

This folder contain a Gradle project that shows how to use the Gatling Kafka DSL with Java.

## Usage

First, start Kafka and the order service from the root folder of this repository:

```console
docker compose up -d
```

Export a token for Gatling's private repositories, from which the plugin is resolved:

```console
export CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token --domain gatlingcorp --domain-owner 049894548288 --region us-east-1 --query authorizationToken --output text)
```

To run the demo simulation, use the `gatlingRun` Gradle task:

```console
./gradlew gatlingRun --simulation io.gatling.kafka.demo.KafkaDemoSimulation
```
