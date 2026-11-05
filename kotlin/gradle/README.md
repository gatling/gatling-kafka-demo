# Gatling Kafka - Kotlin with Gradle demo project

This folder contain a Gradle project that shows how to use the Gatling Kafka DSL with Kotlin.

The simulation places orders with the Gatling Kafka plugin `3.16.1-M2`, on Gatling `3.16.1-M2`: an order is sent with a
non-blocking subscription to the event the order service publishes downstream (`send(...).subscribe(...)`), timed from
the subscription's start, a second order is sent and its confirmation awaited (`requestReply`), then the virtual user
polls its subscriptions (`kafka.poll()`). The [root README](../../README.md) describes the scenario and where the milestones
are resolved from, and the [Gatling Kafka documentation](https://docs.gatling.io/reference/script/kafka/) is not
published yet for this milestone.

## Usage

First, start Kafka and the order service from the root folder of this repository:

```console
docker compose up -d
```

Export a token for Gatling's private repository of public milestones, from which the plugin and Gatling are resolved:

```console
export CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token --domain gatlingcorp --domain-owner 049894548288 --region us-east-1 --query authorizationToken --output text)
```

To run the demo simulation, use the `gatlingRun` Gradle task:

```console
./gradlew gatlingRun --simulation io.gatling.kafka.demo.KafkaDemoSimulation
```
