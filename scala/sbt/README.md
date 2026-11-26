# Gatling Kafka - Scala with SBT demo project

This folder contain a SBT project that shows how to use the Gatling Kafka DSL with Scala.

The simulation places orders with the Gatling Kafka plugin `3.16.1.2`, on Gatling `3.16.1`: an order is sent with a
non-blocking subscription to the event the order service publishes downstream (`send(...).subscribe(...)`), timed from
the subscription's start, a second order is sent and its confirmation awaited (`requestReply`), then the virtual user
polls its subscriptions (`kafka.poll`). The [root README](../../README.md) describes the scenario, and the
[Gatling Kafka documentation](https://docs.gatling.io/reference/script/kafka/) the DSL.

## Usage

First, start Kafka and the order service from the root folder of this repository:

```console
docker compose up -d
```

To run the demo simulation, use the `Gatling / testOnly` SBT task:

```console
sbt "Gatling / testOnly io.gatling.kafka.demo.KafkaDemoSimulation"
```
