package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import io.github.rabbitmq.flow.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.OutboundMessage;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

public class FlowBuilderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowBuilderTest.class);

    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String START_EXCHANGE = "startExchangeName";
    private static final String END_EXCHANGE = "endExchangeName";
    private static final String QUEUE_NAME = "queue";
    private static final String QUEUE_NAME_1 = "queueName1";
    private static final String QUEUE_NAME_2 = "queueName2";

    @Test
    public void createTopic() {
        Flow flow = new FlowBuilder()
                .topic(EXCHANGE_NAME)
                .build();

        flow.start();
    }

    @Test
    public void createTopicPartition() {
        Flow flow = new FlowBuilder()
                .topicPartition(EXCHANGE_NAME)
                .build();

        flow.start();
    }

    @Test
    public void createTopicAndStartPublishing() {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, "", 10);

        Flow flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher))
                .build();

    }

    @Test
    public void createTopicAndStartConsuming() {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, "", 10);

        Flow flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher))
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("#")
                        .queue(QUEUE_NAME)
                        .consumeAutoAck(consume())
                )
                .build();

        flow.start();
    }

    @Test
    public void createsTopicAndStartConsumingExclusivelyInCluster() throws InterruptedException {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, "", 1000);

        Flow flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(EXCHANGE_NAME).atMostOnePublisher(true).publisher(publisher))
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("#")
                        .queue("queueOne")
                        // ensures that at most one consumer is created in a cluster
                        .atMostOne(Duration.ofSeconds(15))
                        .consumeNoAck(consume())
                )
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("#")
                        .queue("queueTwo")

                        .consumeNoAck(consume())
                )
                .build();

        flow.start();
        flow.start();
        Thread.sleep(1000000);
    }

    @Test
    public void createOneToManyRelationshipBetweenTopicAndQueue() {
        Flow flow = new FlowBuilder()
                .topic(toTopic -> toTopic.exchange(EXCHANGE_NAME))
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("1.*")
                        .queue(QUEUE_NAME_1)
                        .consumeNoAck(consume())
                )
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("2.*")
                        .queue(QUEUE_NAME_2)
                        .consumeAutoAck(consume())
                )
                .build();

        flow.start();
    }

    @Test
    public void createTopicPartitionAndStartConsuming() throws InterruptedException {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, () -> UUID.randomUUID().toString(), 100000000);
        Flux<OutboundMessage> publisher2 = TestUtils.outboundMessageFlux(EXCHANGE_NAME, () -> UUID.randomUUID().toString(), 100000000);

        Flow flow = new FlowBuilder()
                .topicPartition(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher))
                .topicPartition(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher2))
                .fromTopicPartition(virtualConsumer -> virtualConsumer
                        .inputExchange(EXCHANGE_NAME)
                        // creates 2 queues `QUEUE_NAME.%d` where %d is in [1,2]
                        .queue(QUEUE_NAME, 5)
                        .buckets(Arrays.asList(1, 2, 1, 1, 1))
                        // for each queue creates `no_instances` consumers
                        // if consumer dies, it is recreated
                        .consumeAutoAck(consume())
                )
                .build();

        flow.start();
        Thread.sleep(100000000);
    }

    @Test
    public void createTopicPartitionAndStartConsumingExclusivelyInCluster() throws InterruptedException {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, () -> UUID.randomUUID().toString(), 100);

        Flow flow = new FlowBuilder()
                .topicPartition(exchange -> exchange.exchange(EXCHANGE_NAME).atMostOnePublisher(true).publisher(publisher))
                .fromTopicPartition(virtualConsumer -> virtualConsumer
                        .inputExchange(EXCHANGE_NAME)
                        // creates 2 queues `QUEUE_NAME.%d` where %d is in [1,2]
                        .queue(QUEUE_NAME, 4)
                        //.buckets(Arrays.asList(1, 2))
                        // ensures through distributed lock that at most one consumer is created in a cluster
                        // it stops consuming after 5 minutes so as not to starve other instances
                        .atMostOne(Duration.ofSeconds(3))
                        .consumeNoAck(consume())
                )
                .build();

        Disposable server1 = flow.start();
        Disposable server2 = flow.start();
        Disposable server3 = flow.start();
        Disposable server4 = flow.start();

        Thread.sleep(10000);
        server1.dispose();
        server2.dispose();
        server3.dispose();

        Thread.sleep(10000000);

    }

    @Test
    public void createShovelAndTheConsume() {
        Flow flow = new FlowBuilder()
                .shovel(shovel -> shovel
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE, ExchangeType.TOPIC)
                        .queue(QUEUE_NAME_1)
                        .routingKey("1.*")
                        .transform(toLowercase())
                )
                .shovel(shovel -> shovel
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE, ExchangeType.TOPIC)
                        .queue(QUEUE_NAME_2)
                        .routingKey("2.*")
                        .transform(toUppercase())
                )
                .fromTopic(consumer -> consumer
                        .inputExchange(END_EXCHANGE)
                        .queue(QUEUE_NAME)
                        .routingKey("#")
                        .consumeNoAck(consume())
                )
                .build();

        flow.start();
    }

    @Test
    public void createShovelPartitionsAndTheConsume() {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(START_EXCHANGE, () -> UUID.randomUUID().toString(), 1000);

        Flow flow = new FlowBuilder()
                .topicPartition(exchange -> exchange.exchange(START_EXCHANGE).publisher(publisher))
                .shovelPartitions(shovelPartition -> shovelPartition
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE, ExchangeType.TOPIC)
                        .queue(QUEUE_NAME_1, 2)
//                        .buckets(Arrays.asList(1, 2))
                        .transform(toLowercase())
                )
                .fromTopic(consumer -> consumer
                        .inputExchange(END_EXCHANGE)
                        .routingKey("#")
                        .queue(QUEUE_NAME_2)
                        .consumeAutoAck(consume())
                )
                .build();

        flow.start();
    }

    private Function<Flux<Delivery>, Flux<Delivery>> consume() {
        return messages -> messages.doOnNext(delivery -> LOGGER.info("Received {}", new String(delivery.getBody())));
    }

    private Function<Flux<Delivery>, Flux<Delivery>> toLowercase() {
        return Function.identity();
    }

    private Function<Flux<Delivery>, Flux<Delivery>> toUppercase() {
        return Function.identity();
    }

}