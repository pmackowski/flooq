package io.github.rabbitmq.flow;

import org.junit.jupiter.api.Test;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

public class FlowTest {

    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String START_EXCHANGE = "startExchangeName";
    private static final String END_EXCHANGE_NAME = "endExchangeName";
    private static final String QUEUE_NAME = "queueName";

    @Test
    public void createsTopicExchangeOnly() {
        Mono<Void> flow = new Flow<String>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME)
                )
                .end();

        flow.subscribe();

    }

    @Test
    public void createsConsistentHashExchangeOnly() {
        Mono<Void> flow = new Flow<String>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME, ExchangeType.CONSISTENT_HASH)
                )
                .end();

        flow.subscribe();

    }

    @Test
    public void createsTopicExchangeAndPublishMessages() {
        Flux<Long> publisher = Flux.interval(Duration.ofSeconds(1));

        Mono<Void> flow = new Flow<Long>()
                .begin(flowBegin -> flowBegin
                    .exchange(EXCHANGE_NAME)
                    .publisher(publisher)
                )
                .end();


        flow.subscribe();
    }

    @Test
    public void createsExchangeAndQueue() {
        Flux<Long> publisher = Flux.interval(Duration.ofSeconds(1));

        Flux<Long> flow = new Flow<Long>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME)
                        .publisher(publisher)
                )
                .end(flowConsume -> flowConsume
                        .inputExchange(EXCHANGE_NAME)
                        .routingKey("a.*")
                        .consume(String.class, consume())
                );

        flow.subscribe();
    }

    @Test
    public void createsConsistentHashExchangeAndVirtualQueue() {
        Flux<Long> publisher = Flux.interval(Duration.ofSeconds(1));

        Flux<Long> flow = new Flow<Long>()
                .begin(flowBegin -> flowBegin
                        .exchange(EXCHANGE_NAME)
                        .publisher(publisher)
                )
                .end(flowConsume -> flowConsume
                        .inputExchange(EXCHANGE_NAME)
                        // creates 5 queues `QUEUE_NAME.%d` where %d is in [1,2,3,4,5]
                        .virtualQueue(QUEUE_NAME, 5)
                        // creates 5 consumers, one consumer per queue
                        // if consumer dies, new one is created
                        .consume(String.class, consume())
                );

        flow.subscribe();
    }


    @Test
    public void flow() {

        Flux<String> publisher = Flux.just("a", "b");

        Flux<String> flow = new Flow<String>()
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
                        .consume(String.class, consume())
                );

        flow.subscribe();
    }

    private Function<Flux<String>, Flux<String>> consume() {
        return messages ->
                messages.doOnNext(System.out::println);
    }

    private Function<String, Publisher<String>> toLowercase() {
        return s -> Mono.just(s).map(String::toLowerCase);
    }

    private Function<String, Publisher<String>> toUppercase() {
        return s -> Mono.just(s).map(String::toUpperCase);
    }

}
