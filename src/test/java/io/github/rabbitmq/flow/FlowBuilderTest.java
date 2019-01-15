package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import io.github.rabbitmq.flow.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

public class FlowBuilderTest {

    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String START_EXCHANGE = "startExchangeName";
    private static final String END_EXCHANGE = "endExchangeName";
    private static final String QUEUE_NAME = "queue";
    private static final String QUEUE_NAME_1 = "queueName1";
    private static final String QUEUE_NAME_2 = "queueName2";

    @Test
    public void createTopic() {
        Mono<Void> flow = new FlowBuilder()
                .topic(EXCHANGE_NAME)
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createTopicPartition() {
        Mono<Void> flow = new FlowBuilder()
                .topicPartition(EXCHANGE_NAME)
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createTopicAndStartPublishing() {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, "", 10);

        Mono<Void> flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher))
                .build();

        StepVerifier.create(flow).verifyComplete();

    }

    @Test
    public void createTopicAndStartConsuming() {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, "", 10);

        Mono<Void> flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher))
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("#")
                        .queue(QUEUE_NAME)
                        .consumeAutoAck(consume())
                )
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createsTopicAndStartConsumingExclusivelyInCluster() {
        Flux<OutboundMessage> publisher = TestUtils.outboundMessageFlux(EXCHANGE_NAME, "", 10);

        Mono<Void> flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(EXCHANGE_NAME).publisher(publisher))
                .fromTopic(consumer -> consumer
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("#")
                        .queue(QUEUE_NAME)
                        // ensures that at most one consumer is created in a cluster
                        .atMostOne()
                        .consumeAutoAck(consume())
                )
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createOneToManyRelationshipBetweenTopicAndQueue() {

        Mono<Void> flow = new FlowBuilder()
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

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createTopicPartitionAndStartConsuming() {
        Mono<Void> flow = new FlowBuilder()
                .fromTopicPartition(virtualConsumer -> virtualConsumer
                        .inputExchange(EXCHANGE_NAME)
                        // creates 2 queues `QUEUE_NAME.%d` where %d is in [1,2]
                        .queue(QUEUE_NAME, 2)
                        .buckets(Arrays.asList(1, 2))
                        // for each queue creates `no_instances` consumers
                        // if consumer dies, it is recreated
                        .consumeAutoAck(consume())
                )
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createTopicPartitionAndStartConsumingExclusivelyInCluster() {
        Mono<Void> flow = new FlowBuilder()
                .fromTopicPartition(virtualConsumer -> virtualConsumer
                        .inputExchange(EXCHANGE_NAME)
                        // creates 2 queues `QUEUE_NAME.%d` where %d is in [1,2]
                        .queue(QUEUE_NAME, 2)
                        .buckets(Arrays.asList(1, 2))
                        // ensures through distributed lock that at most one consumer is created in a cluster
                        // it stops consuming after 5 minutes so as not to starve other instances
                        .atMostOne(Duration.ofMinutes(5))
                        .consumeAutoAck(consume())
                )
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    public void createShovelAndTheConsume() {
        Mono<Void> flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange(START_EXCHANGE))
                .shovel(shovel -> shovel
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE)
                        .queue(QUEUE_NAME_1)
                        .routingKey("1.*")
                        .transform(toLowercase())
                )
                .shovel(shovel -> shovel
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE)
                        .queue(QUEUE_NAME_2)
                        .routingKey("2.*")
                        .transform(toUppercase())
                )
                .fromTopic(consumer -> consumer
                        .inputExchange(END_EXCHANGE)
                        .queue(QUEUE_NAME)
                        .consumeAutoAck(consume())
                )
                .build();

        StepVerifier.create(flow).verifyComplete();
    }

    private Function<Flux<Delivery>, Flux<Delivery>> consume() {
        return messages ->
                messages.doOnNext(System.out::println);
    }

    private Function<OutboundMessage, Publisher<OutboundMessage>> toLowercase() {
        return Mono::just;
    }

    private Function<OutboundMessage, Publisher<OutboundMessage>> toUppercase() {
        return Mono::just;
    }

}