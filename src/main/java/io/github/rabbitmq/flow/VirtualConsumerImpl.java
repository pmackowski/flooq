package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.AcknowledgableDelivery;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class VirtualConsumerImpl implements VirtualConsumer {

    private static final int DEFAULT_BUCKETS_PER_QUEUE = 1;
    private static final int DEFAULT_PARTITIONS_NO = 3;

    private String inputExchange;
    private String queueName;
    private int partitions = DEFAULT_PARTITIONS_NO;
    private List<Integer> buckets = new ArrayList<>();
    private boolean atMostOne = false;
    private Duration leaseTime; // add random part

    private Function<Flux<Delivery>,Flux<Delivery>> consumeNoAck;
    private Function<Flux<Delivery>,Flux<Delivery>> consumeAutoAck;
    private Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consumeManualAck;

    @Override
    public VirtualConsumer inputExchange(String inputExchange) {
        this.inputExchange = inputExchange;
        return this;
    }

    @Override
    public VirtualConsumer queue(String queue, int partitions) {
        this.queueName = queue;
        this.partitions = partitions;
        if (buckets.isEmpty()) {
            buckets.addAll(IntStream.range(0,partitions).map(i -> DEFAULT_BUCKETS_PER_QUEUE).boxed().collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public VirtualConsumer buckets(List<Integer> buckets) {
        this.buckets = buckets;
        return this;
    }

    @Override
    public VirtualConsumer atMostOne() {
        this.atMostOne = true;
        this.leaseTime = Duration.ZERO;
        return this;
    }

    @Override
    public VirtualConsumer atMostOne(Duration leaseTime) {
        this.atMostOne = true;
        this.leaseTime = leaseTime;
        return this;
    }

    @Override
    public VirtualConsumer consumeNoAck(Function<Flux<Delivery>,Flux<Delivery>> consumeNoAck) {
        this.consumeNoAck = consumeNoAck;
        return this;
    }

    @Override
    public VirtualConsumer consumeAutoAck(Function<Flux<Delivery>,Flux<Delivery>> consumeAutoAck) {
        this.consumeAutoAck = consumeAutoAck;
        return this;
    }

    @Override
    public VirtualConsumer consumeManualAck(Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consumeManualAck) {
        this.consumeManualAck = consumeManualAck;
        return this;
    }
}
