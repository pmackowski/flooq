package io.github.flooq;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;

import java.util.function.Function;

class DefaultShovel implements Shovel {

    private String inputExchange;
    private String outputExchange;
    private ExchangeType outputExchangeType;
    private String queue;
    private String routingKey;
    private Function<Flux<Delivery>, Flux<Delivery>> transform;

    @Override
    public Shovel inputExchange(String inputExchange) {
        this.inputExchange = inputExchange;
        return this;
    }

    @Override
    public Shovel outputExchange(String outputExchange) {
        this.outputExchange = outputExchange;
        return this;
    }

    @Override
    public Shovel outputExchange(String outputExchange, ExchangeType exchangeType) {
        this.outputExchange = outputExchange;
        this.outputExchangeType = exchangeType;
        return this;
    }

    @Override
    public Shovel queue(String queue) {
        this.queue = queue;
        return this;
    }

    @Override
    public Shovel routingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    @Override
    public Shovel transform(Function<Flux<Delivery>, Flux<Delivery>> transform) {
        this.transform = transform;
        return this;
    }

    public String getInputExchange() {
        return inputExchange;
    }

    public String getOutputExchange() {
        return outputExchange;
    }

    public ExchangeType getOutputExchangeType() {
        return outputExchangeType;
    }

    public String getQueue() {
        return queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public Function<Flux<Delivery>, Flux<Delivery>> getTransform() {
        return transform;
    }
}
