package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;

public class FlowConsume {

    public FlowConsume inputExchange(String inputExchange) {
        return this;
    }

    public <T> FlowConsume consume(Class<T> inputExchangeType, Publisher<T> publisher) {
        return this;
    }

}
