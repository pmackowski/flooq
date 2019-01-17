package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.AcknowledgableDelivery;

import java.time.Duration;
import java.util.function.Function;

class ConsumerSpecification {

    private String queue;
    private String exchange;
    private boolean atMostOne;
    private Duration leaseTime;
    private Function<Flux<Delivery>,Flux<Delivery>> consumeNoAck;
    private Function<Flux<Delivery>,Flux<Delivery>> consumeAutoAck;
    private Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consumeManualAck;

    public String getQueue() {
        return queue;
    }

    public ConsumerSpecification queue(String queue) {
        this.queue = queue;
        return this;
    }

    public String getExchange() {
        return exchange;
    }

    public ConsumerSpecification exchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public boolean isAtMostOne() {
        return atMostOne;
    }

    public ConsumerSpecification atMostOne(boolean atMostOne) {
        this.atMostOne = atMostOne;
        return this;
    }

    public Duration getLeaseTime() {
        return leaseTime;
    }

    public ConsumerSpecification leaseTime(Duration leaseTime) {
        this.leaseTime = leaseTime;
        return this;
    }

    public Function<Flux<Delivery>, Flux<Delivery>> getConsumeNoAck() {
        return consumeNoAck;
    }

    public ConsumerSpecification consumeNoAck(Function<Flux<Delivery>, Flux<Delivery>> consumeNoAck) {
        this.consumeNoAck = consumeNoAck;
        return this;
    }

    public Function<Flux<Delivery>, Flux<Delivery>> getConsumeAutoAck() {
        return consumeAutoAck;
    }

    public ConsumerSpecification consumeAutoAck(Function<Flux<Delivery>, Flux<Delivery>> consumeAutoAck) {
        this.consumeAutoAck = consumeAutoAck;
        return this;
    }

    public Function<Flux<AcknowledgableDelivery>, Flux<AcknowledgableDelivery>> getConsumeManualAck() {
        return consumeManualAck;
    }

    public ConsumerSpecification consumeManualAck(Function<Flux<AcknowledgableDelivery>, Flux<AcknowledgableDelivery>> consumeManualAck) {
        this.consumeManualAck = consumeManualAck;
        return this;
    }
}
