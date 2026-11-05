package io.gatling.kafka.demo;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.kafka.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.kafka.KafkaDsl.*;

public class KafkaDemoSimulation extends Simulation {

  // Start Kafka and the order service first with: docker compose up -d
  // (from the root folder of this repository)
  KafkaProtocolBuilder kafkaProtocol = kafka
    .bootstrapServers("localhost:9092")
    // Kafka 4 producers wait up to 5 ms (linger.ms) to batch records, which adds up to 5 ms to every send:
    // send each record right away instead, as this test measures latency, not throughput
    .producerProperty("linger.ms", "0")
    // a reply, or an expected record, is the one with the same key as the order
    .matchByKey()
    .replyTimeout(Duration.ofSeconds(10));

  // a new order for each feed
  Iterator<Map<String, Object>> orders = Stream.generate(() -> {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int quantity = random.nextInt(1, 6);
    int unitPriceCents = random.nextInt(500, 5000);
    return Map.<String, Object>of(
      "orderId", UUID.randomUUID().toString(),
      "productId", "SKU-" + random.nextInt(1, 100),
      "quantity", quantity,
      "unitPriceCents", unitPriceCents,
      "totalCents", quantity * unitPriceCents
    );
  }).iterator();

  Body.WithString order = StringBody(
    "{\"orderId\": \"#{orderId}\", \"productId\": \"#{productId}\", \"quantity\": #{quantity}, \"unitPriceCents\": #{unitPriceCents}}"
  );

  // fire and forget: the order is sent, timed until the broker acknowledges it,
  // then the event the order service publishes downstream is expected
  ChainBuilder placeOrder = feed(orders)
    .exec(session -> session.set("sentAt", System.currentTimeMillis()))
    .exec(
      kafka("Place order")
        .send("orders")
        .value(order)
        .key("#{orderId}")
        .header("channel", "web"),
      kafka("Order shipped")
        .expect("order-events")
        .correlationId("#{orderId}")
        // timed from the send until the event is read: the latency of the whole pipeline
        .startTimestampFrom("sentAt")
        .check(jsonPath("$.type").is("ORDER_SHIPPED"))
    );

  // request-reply: the order is sent, timed until its confirmation is read
  ChainBuilder placeOrderAndWaitForConfirmation = feed(orders)
    .exec(
      kafka("Place order and wait for confirmation")
        .requestReply("orders", "order-confirmations")
        .value(order)
        .key("#{orderId}")
        .header("channel", "web")
        .check(
          jsonPath("$.orderId").isEL("#{orderId}"),
          jsonPath("$.status").is("ACCEPTED"),
          jsonPath("$.totalCents").ofInt().isEL("#{totalCents}"),
          kafkaHeader("channel").is("web")
        )
    );

  ScenarioBuilder scn = scenario("Orders")
    .during(Duration.ofMinutes(1)).on(
      exec(placeOrder)
        .pause(1)
        .exec(placeOrderAndWaitForConfirmation)
        .pause(1)
    );

  // ./gradlew gatlingRun --simulation io.gatling.kafka.demo.KafkaDemoSimulation

  {
    setUp(
      scn.injectOpen(atOnceUsers(5))
    ).protocols(kafkaProtocol);
  }
}
