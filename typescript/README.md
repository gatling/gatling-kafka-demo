# Gatling Kafka - TypeScript demo project

This folder contain a TypeScript project that shows how to use the Gatling Kafka DSL.

The simulation places orders with `@gatling.io/kafka` `3.16.101`: an order is sent with a non-blocking subscription
to the event the order service publishes downstream (`send(...).subscribe(...)`), timed from the subscription's start,
a second order is sent and its confirmation awaited (`requestReply`), then the virtual user polls its subscriptions
(`kafka.poll()`). The [root README](../README.md) describes the scenario, and the
[Gatling Kafka documentation](https://docs.gatling.io/reference/script/kafka/) the DSL.

## Usage

First, start Kafka and the order service from the root folder of this repository:

```console
docker compose up -d
```

To run the demo simulation, use the `run` command:

```console
npm install
npx gatling run --simulation kafka
```
