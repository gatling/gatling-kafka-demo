# Gatling Kafka - Java with Maven demo project

This folder contain a Maven project that shows how to use the Gatling Kafka DSL with Java.

## Usage

First, start Kafka and the order service from the root folder of this repository:

```console
docker compose up -d
```

To run the demo simulation, use the `gatling:test` Maven goal:

```console
mvn gatling:test -Dgatling.simulationClass=io.gatling.kafka.demo.KafkaDemoSimulation
```
