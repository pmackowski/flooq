package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.AcknowledgableDelivery;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public interface VirtualConsumer {

    VirtualConsumer inputExchange(String inputExchange);

    VirtualConsumer queue(String queue, int partitions);

    VirtualConsumer buckets(List<Integer> buckets);

    VirtualConsumer atMostOne();

    VirtualConsumer atMostOne(Duration leaseTime);

    VirtualConsumer consumeNoAck(Function<Flux<Delivery>,Flux<Delivery>> consumeNoAck);

    VirtualConsumer consumeAutoAck(Function<Flux<Delivery>,Flux<Delivery>> consumeAutoAck);

    VirtualConsumer consumeManualAck(Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consumeManualAck);

}
