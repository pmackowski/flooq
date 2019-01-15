package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.AcknowledgableDelivery;

import java.time.Duration;
import java.util.function.Function;

public interface Consumer {

    Consumer inputExchange(String inputExchange);

    Consumer queue(String queueName);

    /**
     * By default all messages
     * @param routingKey
     * @return
     */
    Consumer routingKey(String routingKey);

    Consumer atMostOne();

    Consumer atMostOne(Duration leaseTime);

    Consumer consumeNoAck(Function<Flux<Delivery>,Flux<Delivery>> consume);

    Consumer consumeAutoAck(Function<Flux<Delivery>,Flux<Delivery>> consume);

    Consumer consumeManualAck(Function<Flux<AcknowledgableDelivery>,Flux<AcknowledgableDelivery>> consume);
}
