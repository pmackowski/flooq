package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;

import java.util.function.Function;

public class FlowRoute {

    public <T,W> FlowRoute transform(Class<T> inputExchangeType, Class<W> outputExchangeType, Function<T, Publisher<W>> router) {
        return this;
    }

    public FlowRoute errorOutputExchange(String errorOutputExchange) {
        return this;
    }

    public FlowRoute inputExchange(String inputExchange) {
        return this;
    }

    public FlowRoute outputExchange(String outputExchange) {
        return this;
    }

    public FlowRoute inputExchangeType(Class<?> inputExchangeType) {
        return this;
    }
    public FlowRoute outputExchangeType(Class<?> outputExchangeType) {
        return this;
    }

    public FlowRoute routingKey(String routingKey) {
        return this;
    }
}
