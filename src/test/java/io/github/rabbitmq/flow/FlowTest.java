package io.github.rabbitmq.flow;

import io.github.rabbitmq.flow.utils.TestFlowEvent;
import io.github.rabbitmq.flow.utils.TestUtils;
import org.junit.jupiter.api.Test;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Function;

public class FlowTest {
    // {"i":0,"exchange":"exchangeName","routingKey":""}
    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String START_EXCHANGE = "startExchangeName";
    private static final String END_EXCHANGE_NAME = "endExchangeName";
    private static final String QUEUE_NAME = "queueName";

    @Test
    public void createsTopicExchangeOnly() {
        Mono<Void> flow = new Flow<TestFlowEvent>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME)
                )
                .end();

        StepVerifier.create(flow).verifyComplete();

    }

    @Test
    public void createsConsistentHashExchangeOnly() {
        Mono<Void> flow = new Flow<TestFlowEvent>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME, ExchangeType.CONSISTENT_HASH)
                )
                .end();

        StepVerifier.create(flow).verifyComplete();

    }

    @Test
    public void createsTopicExchangeAndPublishMessages() {
        Flux<TestFlowEvent> publisher = TestUtils.flowEventFlux(EXCHANGE_NAME, "", 10);

        Mono<Void> flow = new Flow<TestFlowEvent>()
                .begin(flowBegin -> flowBegin
                    .exchange(EXCHANGE_NAME)
                    .publisher(publisher)
                )
                .end();

        StepVerifier.create(flow).verifyComplete();

    }

    @Test
    public void createsExchangeAndQueue() {
        Flux<TestFlowEvent> publisher = TestUtils.flowEventFlux(EXCHANGE_NAME, "", 10);

        StepVerifier.create(new Flow<TestFlowEvent>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME)
                        .publisher(publisher)
                )
                .end(flowConsume -> flowConsume
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("#")
                        .queueName(QUEUE_NAME)
                        .type(TestFlowEvent.class)
                        .consume(consume())
                )
        ).expectNextCount(10)
          .thenCancel()
                .verify();
    }

    @Test
    public void createsConsistentHashExchangeAndVirtualQueue() {
        Flux<TestFlowEvent> publisher = TestUtils.flowEventFlux(EXCHANGE_NAME, "", 10);

        Flux<TestFlowEvent> flow = new Flow<TestFlowEvent>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME, ExchangeType.CONSISTENT_HASH)
                        //.publisher(publisher)
                )
                .end(flowConsume -> flowConsume
                        .inputExchange(EXCHANGE_NAME)
                        .type(TestFlowEvent.class)
                        // creates 3 queues `QUEUE_NAME.%d` where %d is in [1,2,3]
                        .virtualQueue(QUEUE_NAME, 3)
                        // creates 3 consumers, one consumer per queue
                        // if consumer dies, new one is created
                        .consume(consume())
                );

        StepVerifier.create(flow)
                .thenAwait(Duration.ofMinutes(5))
                .thenCancel()
                .verify();
    }


    @Test
    public void flow() {

        Flux<TestFlowEvent> publisher = TestUtils.flowEventFlux(EXCHANGE_NAME, "", 10);

        Flux<TestFlowEvent> flow = new Flow<TestFlowEvent>()
                .begin(flowBegin -> flowBegin
                        .exchange(START_EXCHANGE)
                        .publisher(publisher)
                )

                .route(flowRoute -> flowRoute
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE_NAME)
                        .routingKey("a.*")
                        .transform(String.class, String.class, toLowercase())
                )

                .route(flowRoute -> flowRoute
                        .inputExchange(START_EXCHANGE)
                        .outputExchange(END_EXCHANGE_NAME)
                        .routingKey("b.*")
                        .transform(String.class, String.class, toUppercase())
                )

                .end(flowConsume -> flowConsume
                        .inputExchange(END_EXCHANGE_NAME)
                        .consume(consume())
                );

        flow.subscribe();
    }

    private Function<Flux<TestFlowEvent>, Flux<TestFlowEvent>> consume() {
        return messages ->
                messages.doOnNext(System.out::println).doOnError(throwable -> System.out.println("ss " + throwable));
    }

    private Function<String, Publisher<String>> toLowercase() {
        return s -> Mono.just(s).map(String::toLowerCase);
    }

    private Function<String, Publisher<String>> toUppercase() {
        return s -> Mono.just(s).map(String::toUpperCase);
    }

}
