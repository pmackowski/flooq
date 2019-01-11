package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;

public class FlowBegin<T> {

    private String exchangeName;
    private ExchangeType exchangeType;
    private Publisher<T> publisher;

    public FlowBegin<T> exchange(String exchangeName) {
        return exchange(exchangeName, ExchangeType.TOPIC);
    }

    public FlowBegin<T> exchange(String exchangeName, ExchangeType exchangeType) {
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
        return this;
    }

    public FlowBegin<T> publisher(Publisher<T> publisher) {
        this.publisher = publisher;
        return this;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public Publisher<T> getPublisher() {
        return publisher;
    }
}
