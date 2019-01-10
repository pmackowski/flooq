package io.github.rabbitmq.flow;

import org.junit.jupiter.api.Test;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

public class FlowTest {

    @Test
    public void flowCreatesExchangeOnly() {

        Mono<Void> flow = new Flow<String>()
                .begin(flowBegin -> flowBegin
                        .exchangeName("start")
                )
                .end();

        flow.subscribe();

    }

    @Test
    public void flowCreatesExchangeAndPublishMessages() {
        Flux<Long> publisher = Flux.interval(Duration.ofSeconds(1));

        Mono<Void> flow = new Flow<Long>()
                .begin(flowBegin -> flowBegin
                    .exchangeName("start")
                    .publisher(publisher)
                )
                .end();


        flow.subscribe();
    }

    @Test
    public void flowCreatesExchangeAndQueue() {
        Flux<Long> publisher = Flux.interval(Duration.ofSeconds(1));

        Flux<Long> flow = new Flow<Long>()
                .begin(flowBegin -> flowBegin
                        .exchangeName("start")
                        .publisher(publisher)
                )
                .end(flowConsume -> flowConsume
                        .inputExchange("start")
                        .consume(String.class, finish())
                );

        flow.subscribe();
    }

    @Test
    public void flow() {

        Flux<String> publisher = Flux.just("a", "b");

        Flux<String> flow = new Flow<String>()
                .begin(flowBegin -> flowBegin
                        .exchangeName("start")
                        .publisher(publisher)
                )

                .route(flowRoute -> flowRoute
                        .inputExchange("start")
                        .outputExchange("end")
                        .errorOutputExchange("end.error")
                        .routingKey("a.*")
                        .transform(String.class, String.class, toLowercase())
                )

                .route(flowRoute -> flowRoute
                        .inputExchange("start")
                        .outputExchange("end")
                        .errorOutputExchange("end.error")
                        .routingKey("b.*")
                        .transform(String.class, String.class, toUppercase())
                )

                .end(flowConsume -> flowConsume
                        .inputExchange("end")
                        .consume(String.class, finish())
                );

        flow.subscribe();
    }

    private Function<String, Publisher<String>> toLowercase() {
        return s -> Mono.just(s).map(String::toLowerCase);
    }

    private Function<String, Publisher<String>> toUppercase() {
        return s -> Mono.just(s).map(String::toUpperCase);
    }

    private Publisher<String> finish() {
        return str -> {};
    }

}
