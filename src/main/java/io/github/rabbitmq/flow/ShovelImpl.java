package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import reactor.rabbitmq.OutboundMessage;

import java.util.function.Function;

class ShovelImpl implements Shovel {

    private String inputExchange;
    private String outputExchange;
    private String queue;
    private String routingKey;
    private Function<OutboundMessage, Publisher<OutboundMessage>> transform;

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
    public Shovel transform(Function<OutboundMessage, Publisher<OutboundMessage>> transform) {
        this.transform = transform;
        return this;
    }

}
