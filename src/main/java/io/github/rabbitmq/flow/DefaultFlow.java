package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class DefaultFlow implements Flow {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFlow.class);

    private final FlowOptions flowOptions;
    private final AmqpEntities amqpEntities;

    DefaultFlow(FlowOptions flowOptions, AmqpEntities amqpEntities) {
        this.flowOptions = flowOptions;
        this.amqpEntities = amqpEntities;
    }

    @Override
    public Mono<Void> start() {
        Sender sender = flowOptions.getSender();

        Receiver receiver = flowOptions.getReceiver();

        List<Publisher<?>> publishers = new ArrayList<>();

        List<Publisher<?>> consumers = amqpEntities.getConsumerSpecifications().stream().map(
                consumerSpecification -> {
                    Flux<? extends Delivery> transform;
                    if (consumerSpecification.getConsumeNoAck() != null) {
                        transform = receiver.consumeNoAck(consumerSpecification.getQueue())
                                .doOnNext(s -> LOGGER.info("Consuming on quqeue {} {}", consumerSpecification.getQueue(), new String(s.getBody())))
                                .transform(consumerSpecification.getConsumeNoAck());
                    } else if (consumerSpecification.getConsumeAutoAck() != null) {
                        transform = receiver.consumeAutoAck(consumerSpecification.getQueue())
                                .doOnNext(s -> LOGGER.info("Consuming on quqeue {} {}", consumerSpecification.getQueue(), new String(s.getBody())))
                                .transform(consumerSpecification.getConsumeAutoAck());
                    } else {
                        transform = receiver.consumeManualAck(consumerSpecification.getQueue())
                                .doOnNext(s -> LOGGER.info("Consuming on quqeue {} {}", consumerSpecification.getQueue(), new String(s.getBody())))
                                .transform(consumerSpecification.getConsumeManualAck());
                    }
                    if (consumerSpecification.getExchange() != null) {
                        return transform.flatMap(delivery -> sender.send(Mono.just(
                                new OutboundMessage(consumerSpecification.getExchange(), delivery.getEnvelope().getRoutingKey(), delivery.getBody())
                        )));
                    }
                    return transform;
                }
        ).collect(Collectors.toList());

        publishers.addAll(consumers);
        publishers.addAll(amqpEntities.getPublishers().stream().map(sender::sendWithPublishConfirms).collect(Collectors.toList()));

        return Flux.fromIterable(amqpEntities.getExchangeSpecifications())
            .flatMap(sender::declareExchange)
            .thenMany(Flux.fromIterable(amqpEntities.getQueueSpecifications()))
            .flatMap(sender::declareQueue)
            .thenMany(Flux.fromIterable(amqpEntities.getBindingSpecifications()))
            .flatMap(sender::bind)

            .thenMany(Flux.fromIterable(publishers).flatMap(s -> s))
            .then();
    }
}
