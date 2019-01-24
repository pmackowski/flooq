package io.github.rabbitmq.flow.samples;

import io.github.rabbitmq.flow.Flow;
import io.github.rabbitmq.flow.FlowBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.OutboundMessage;

import java.time.Duration;

public class SampleFlow {

    public static void main(String[] args) {
        Flux<OutboundMessage> publisher =  Flux.range(0, 10)
                .delayElements(Duration.ofSeconds(1))
                .map(i -> new OutboundMessage("my.exchange", "", "".getBytes()));

        Flow flow = new FlowBuilder()
                .topic(exchange -> exchange.exchange("my.exchange").publisher(publisher))
                .fromTopic(consumer -> consumer
                        .inputExchange("my.exchange")
                        .queue("my.queue")
                        .routingKey("#")
                        .consumeNoAck(messages -> messages)
                )
                .build();

        Disposable disposable = flow.start();
    }

}
