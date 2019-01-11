package io.github.rabbitmq.flow;

import reactor.core.publisher.Flux;

import java.util.function.Function;

public class FlowConsume {

    public FlowConsume inputExchange(String inputExchange) {
        return this;
    }

    public FlowConsume routingKey(String routingKey) {
        return this;
    }

    public FlowConsume queueName(String queueName) {
        return this;
    }

    public FlowConsume virtualQueue(String queueName, int innerQueues) {
        return this;
    }

    public <T> FlowConsume consume(Class<T> inputExchangeType, Function<Flux<T>,Flux<T>> func) {
        return this;
    }

}
