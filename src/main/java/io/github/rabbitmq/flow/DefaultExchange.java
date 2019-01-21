package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;

class DefaultExchange implements Exchange {

    private String exchange;
    private ExchangeType exchangeType = ExchangeType.TOPIC;
    private Publisher<OutboundMessage> publisher = Mono.empty();
    private boolean atMostOnePublisher;

    @Override
    public Exchange exchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    @Override
    public Exchange exchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
        return this;
    }

    public Exchange exchange(String exchangeName, ExchangeType exchangeType) {
        this.exchange = exchangeName;
        this.exchangeType = exchangeType;
        return this;
    }

    @Override
    public Exchange publisher(Publisher<OutboundMessage> publisher) {
        this.publisher = publisher;
        return this;
    }

    @Override
    public Exchange atMostOnePublisher(boolean atMostOnePublisher) {
        this.atMostOnePublisher = atMostOnePublisher;
        return this;
    }

    public String getExchange() {
        return exchange;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public Publisher<OutboundMessage> getPublisher() {
        return publisher;
    }

    public boolean isAtMostOnePublisher() {
        return atMostOnePublisher;
    }
}
