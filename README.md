# Gatling Kafka demo

This demo project contains all combination of currently supported languages and build tools:

- [Java with Gradle](https://github.com/gatling/gatling-kafka-demo/tree/main/java/gradle)
- [Java with Maven](https://github.com/gatling/gatling-kafka-demo/tree/main/java/maven)
- [Kotlin with Gradle](https://github.com/gatling/gatling-kafka-demo/tree/main/kotlin/gradle)
- [Kotlin with Maven](https://github.com/gatling/gatling-kafka-demo/tree/main/kotlin/maven)
- [Scala with SBT](https://github.com/gatling/gatling-kafka-demo/tree/main/scala/sbt)

JavaScript and TypeScript are not supported.

The simulations run against a small order service, in a local Kafka broker started with Docker:

```console
docker compose up -d
```

It starts a single Kafka broker (KRaft) that the simulations reach on `localhost:9092`, creates the `orders`,
`order-confirmations` and `order-events` topics, and starts the order service: a [Redpanda Connect](https://docs.redpanda.com/redpanda-connect/)
pipeline, [order-service/connect.yaml](order-service/connect.yaml), that reads each order from `orders` and writes a
confirmation on `order-confirmations` and an event on `order-events`, both with the order's record key and its
`channel` header. Stop them with `docker compose down`.

Each virtual user places orders for a minute, as JSON records with the order id as their key and a `channel` header:

- fire and forget, with a subscription: right before sending an order (`send`), timed until the broker acknowledges
  it, it subscribes to the order service's event on `order-events` (`subscribe`), without waiting for it. The
  subscription is timed from when it starts until the event is read: the latency of the whole pipeline;
- request-reply: it sends an order and waits for its confirmation on `order-confirmations`, matched by key
  (`requestReply`), then checks it;
- then it polls its subscriptions (`kafka.poll`, `kafka.poll()` in Java and Kotlin): it waits for the shipped event of the first order if it is not in
  yet, and the event's check failure, if any, applies to the virtual user.

A subscription is the pattern to copy for a Kafka-to-Kafka pipeline: the virtual user goes on while the record it
subscribed to travels through the system. `consume` is the blocking alternative, which waits for the record where it
is placed.

This version of the plugin, `3.16.1-M2`, is a milestone built against Gatling `3.16.1-M2`, which is a milestone too:
neither is on Maven Central, and both are resolved from Gatling's private repository of public milestones (`staging`).
The HTML reports' charts (`gatling-charts-highcharts`) have no milestone, so the projects keep their latest release,
`3.16.0`. Maven reads the repository's credentials from `~/.m2/settings.xml`, sbt from a global sbt file in
`~/.config/sbt/2/`, and Gradle from the `CODEARTIFACT_AUTH_TOKEN` environment variable:

```console
export CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token --domain gatlingcorp --domain-owner 049894548288 --region us-east-1 --query authorizationToken --output text)
```

Check each project for their usage details, and the [Gatling Kafka documentation](https://docs.gatling.io/reference/script/kafka/),
which is not published yet for this milestone.
