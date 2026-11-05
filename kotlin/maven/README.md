# Gatling Kafka - Kotlin with Maven demo project

This folder contain a Maven project that shows how to use the Gatling Kafka DSL with Kotlin.

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

To run the demo simulation, use the `gatling:test` Maven goal:

```console
mvn gatling:test -Dgatling.simulationClass=io.gatling.kafka.demo.KafkaDemoSimulation
```
