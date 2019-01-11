package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;

public class FlowBegin<T> {

    public FlowBegin<T> exchange(String exchangeName) {
        return exchange(exchangeName, ExchangeType.TOPIC);
    }

    public FlowBegin<T> exchange(String exchangeName, ExchangeType exchangeType) {
        return this;
    }

    public FlowBegin<T> publisher(Publisher<T> publisher) {
        return this;
    }


}
