package io.gatling.kafka.demo

import java.time.Duration
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.kafka.KafkaDsl.*

class KafkaDemoSimulation : Simulation() {

  // Start Kafka and the order service first with: docker compose up -d
  // (from the root folder of this repository)
  val kafkaProtocol = kafka
    .bootstrapServers("localhost:9092")
    // Kafka 4 producers wait up to 5 ms (linger.ms) to batch records, which adds up to 5 ms to every send:
    // send each record right away instead, as this test measures latency, not throughput
    .producerProperty("linger.ms", "0")
    // a reply, or a subscribed record, is the one with the same key as the order
    .matchByKey()
    .replyTimeout(Duration.ofSeconds(10))

  // a new order for each feed
  val orders = generateSequence {
    val random = ThreadLocalRandom.current()
    val quantity = random.nextInt(1, 6)
    val unitPriceCents = random.nextInt(500, 5000)
    mapOf<String, Any>(
      "orderId" to UUID.randomUUID().toString(),
      "productId" to "SKU-${random.nextInt(1, 100)}",
      "quantity" to quantity,
      "unitPriceCents" to unitPriceCents,
      "totalCents" to quantity * unitPriceCents
    )
  }.iterator()

  val order = StringBody(
    """{"orderId": "#{orderId}", "productId": "#{productId}", "quantity": #{quantity}, "unitPriceCents": #{unitPriceCents}}"""
  )

  // fire and forget: the order is sent, timed until the broker acknowledges it.
  // Right before sending, it subscribes to the event the order service publishes downstream on order-events,
  // without blocking: the virtual user goes on, and polls the subscription later
  val placeOrder = feed(orders)
    .exec(
      kafka("Place order")
        .send("orders")
        .value(order)
        .key("#{orderId}")
        .header("channel", "web")
        .subscribe(
          // timed from when the subscription starts, right before the send, until the event is read:
          // the latency of the whole pipeline
          kafka("Order shipped")
            .subscribe("order-events")
            .correlationId("#{orderId}")
            .check(jsonPath("$.type").shouldBe("ORDER_SHIPPED"))
        )
    )

  // request-reply: the order is sent, timed until its confirmation is read
  val placeOrderAndWaitForConfirmation = feed(orders)
    .exec(
      kafka("Place order and wait for confirmation")
        .requestReply("orders", "order-confirmations")
        .value(order)
        .key("#{orderId}")
        .header("channel", "web")
        .check(
          jsonPath("$.orderId").isEL("#{orderId}"),
          jsonPath("$.status").shouldBe("ACCEPTED"),
          jsonPath("$.totalCents").ofInt().isEL("#{totalCents}"),
          kafkaHeader("channel").shouldBe("web")
        )
    )

  val scn = scenario("Orders")
    .during(Duration.ofMinutes(1)).on(
      exec(placeOrder)
        .pause(1)
        .exec(placeOrderAndWaitForConfirmation)
        // waits for the order shipped event subscribed to above, if it is not in yet,
        // and applies its check's failure to the virtual user
        .exec(kafka.poll())
        .pause(1)
    )

  // ./gradlew gatlingRun --simulation io.gatling.kafka.demo.KafkaDemoSimulation

  init {
    setUp(
      scn.injectOpen(atOnceUsers(5))
    ).protocols(kafkaProtocol)
  }
}
