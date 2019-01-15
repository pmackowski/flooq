package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.AcknowledgableDelivery;

import java.time.Duration;
import java.util.function.Function;

class ConsumerImpl implements Consumer {

    private String inputExchange;
    private String routingKey;
    private String queue;
    private boolean atMostOneConsumer;
    private Duration leaseTime; // add random part

    private Function<Flux<Delivery>,Flux<Delivery>> consumeNoAck;
    private Function<Flux<Delivery>,Flux<Delivery>> consumeAutoAck;
    private Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consumeManualAck;

    @Override
    public Consumer inputExchange(String inputExchange) {
        this.inputExchange = inputExchange;
        return this;
    }

    @Override
    public Consumer routingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    @Override
    public Consumer queue(String queueName) {
        this.queue = queueName;
        return this;
    }

    @Override
    public Consumer atMostOne() {
        this.atMostOneConsumer = true;
        this.leaseTime = Duration.ZERO;
        return this;
    }

    @Override
    public Consumer atMostOne(Duration leaseTime) {
        this.atMostOneConsumer = true;
        this.leaseTime = leaseTime;
        return this;
    }

    @Override
    public Consumer consumeNoAck(Function<Flux<Delivery>,Flux<Delivery>> consumeNoAck) {
        this.consumeNoAck = consumeNoAck;
        return this;
    }

    @Override
    public Consumer consumeAutoAck(Function<Flux<Delivery>,Flux<Delivery>> consumeAutoAck) {
        this.consumeAutoAck = consumeAutoAck;
        return this;
    }

    @Override
    public Consumer consumeManualAck(Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consumeManualAck) {
        this.consumeManualAck = consumeManualAck;
        return this;
    }

}
