package io.github.rabbitmq.flow;

import reactor.core.publisher.Flux;
import reactor.rabbitmq.Sender;

class DefaultFlow implements Flow {

    private final FlowOptions flowOptions;
    private final AmqpEntities amqpEntities;

    DefaultFlow(FlowOptions flowOptions, AmqpEntities amqpEntities) {
        this.flowOptions = flowOptions;
        this.amqpEntities = amqpEntities;
    }

    @Override
    public void start() {
        Sender sender = flowOptions.getSender();

        Flux.fromIterable(amqpEntities.getExchangeSpecifications())
            .flatMap(sender::declareExchange)
            .thenMany(Flux.fromIterable(amqpEntities.getQueueSpecifications()))
            .flatMap(sender::declareQueue)
            .thenMany(Flux.fromIterable(amqpEntities.getBindingSpecifications()))
            .flatMap(sender::bind)
            .blockLast();


    }
}
