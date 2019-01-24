package io.github.flooq.samples;

import io.github.flooq.Flooq;
import io.github.flooq.FlooqBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.OutboundMessage;

import java.time.Duration;

public class SampleFlooq {

    public static void main(String[] args) {
        Flux<OutboundMessage> publisher =  Flux.range(0, 10)
                .delayElements(Duration.ofSeconds(1))
                .map(i -> new OutboundMessage("my.exchange", "", "".getBytes()));

        Flooq flooq = new FlooqBuilder()
                .topic(exchange -> exchange.exchange("my.exchange").publisher(publisher))
                .fromTopic(consumer -> consumer
                        .inputExchange("my.exchange")
                        .queue("my.queue")
                        .routingKey("#")
                        .consumeNoAck(messages -> messages)
                )
                .build();

        Disposable disposable = flooq.start();
    }

}
