import { StringBody, atOnceUsers, exec, jsonPath, scenario, simulation } from "@gatling.io/core";
import { kafka, kafkaHeader } from "@gatling.io/kafka";
import * as uuid from "uuid";

export default simulation((setUp) => {
  // Start Kafka and the order service first with: docker compose up -d
  // (from the root folder of this repository)
  const kafkaProtocol = kafka
    .bootstrapServers("localhost:9092")
    // Kafka 4 producers wait up to 5 ms (linger.ms) to batch records, which adds up to 5 ms to every send:
    // send each record right away instead, as this test measures latency, not throughput
    .producerProperty("linger.ms", "0")
    // a reply, or a subscribed record, is the one with the same key as the order
    .matchByKey()
    .replyTimeout({ amount: 10, unit: "seconds" });

  // a random integer between min (inclusive) and max (exclusive)
  const randomInt = (min, max) => min + Math.floor(Math.random() * (max - min));

  // a new order each time
  const newOrder = exec((session) => {
    const quantity = randomInt(1, 6);
    const unitPriceCents = randomInt(500, 5000);
    return session.setAll({
      orderId: uuid.v4(),
      productId: "SKU-" + randomInt(1, 100),
      quantity,
      unitPriceCents,
      totalCents: quantity * unitPriceCents
    });
  });

  const order = StringBody(
    '{"orderId": "#{orderId}", "productId": "#{productId}", "quantity": #{quantity}, "unitPriceCents": #{unitPriceCents}}'
  );

  // fire and forget: the order is sent, timed until the broker acknowledges it.
  // Right before sending, it subscribes to the event the order service publishes downstream on order-events,
  // without blocking: the virtual user goes on, and polls the subscription later
  const placeOrder = exec(
    newOrder,
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
          .check(jsonPath("$.type").is("ORDER_SHIPPED"))
      )
  );

  // request-reply: the order is sent, timed until its confirmation is read
  const placeOrderAndWaitForConfirmation = exec(
    newOrder,
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

  const scn = scenario("Orders").during({ amount: 1, unit: "minutes" }).on(
    exec(placeOrder)
      .pause(1)
      .exec(placeOrderAndWaitForConfirmation)
      // waits for the order shipped event subscribed to above, if it is not in yet,
      // and applies its check's failure to the virtual user
      .exec(kafka.poll())
      .pause(1)
  );

  // npx gatling run --simulation kafka

  setUp(scn.injectOpen(atOnceUsers(5))).protocols(kafkaProtocol);
});
