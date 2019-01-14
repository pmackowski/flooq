package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.BindingSpecification;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.function.Function;

public class RabbitMqRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqRouter.class);

    private final RabbitMqReceiver receiver;
    private final RabbitMqSender sender;
    private final RetryAcknowledgement retryAcknowledgement;

    public RabbitMqRouter(RabbitMqReceiver receiver, RabbitMqSender sender, RetryAcknowledgement retryAcknowledgement) {
        this.receiver = receiver;
        this.sender = sender;
        this.retryAcknowledgement = retryAcknowledgement;
    }

    public <S extends FlowEvent, T extends FlowEvent> void route(RouterSpecification routerSpecification, Class<S> intermediateQueueMessageType, Function<S, Publisher<T>> router) {
        LOGGER.info("Router has been enabled {}", routerSpecification);
        routeFlux(routerSpecification, intermediateQueueMessageType, router).subscribe();
    }

    <S extends FlowEvent, T extends FlowEvent> Flux<Tuple2<FlowEventAckDelivery<S>, List<T>>> routeFlux(RouterSpecification routerSpecification, Class<S> intermediateQueueMessageType, Function<S, Publisher<T>> router) {
        BindingSpecification inputBindingSpecification = createFrom(routerSpecification);

        return receiver.consumeManualAck(inputBindingSpecification, intermediateQueueMessageType)
                .flatMapSequential(delivery -> transformMessage(delivery, routerSpecification, router))
                .concatMap(tuple -> confirm(routerSpecification, tuple));
                // retry recreate consumer
    }

    private <S extends FlowEvent, T extends FlowEvent> Flux<Tuple2<FlowEventAckDelivery<S>, List<T>>> transformMessage(FlowEventAckDelivery<S> delivery, RouterSpecification routerSpecification, Function<S, Publisher<T>> router) {
        return Flux.from(router.apply(delivery.getMessage()))
                .doOnError(throwable -> {
                    // TODO send to error output exchange ?? or just use dead-letter-exchange
                })
                .buffer()
                .map(events -> Tuples.of(delivery, events))
                .onErrorResume(exc -> {
                    LOGGER.error("Message in queue {} cannot be processed and will be moved to dead letter queue! Message body is\n {}",
                            routerSpecification.getIntermediateQueue(), delivery.getMessage(), exc);
                    return Flux.empty();
                });
    }

    private <S extends FlowEvent, T extends FlowEvent> Publisher<Tuple2<FlowEventAckDelivery<S>, List<T>>> confirm(RouterSpecification routerSpecification, Tuple2<FlowEventAckDelivery<S>, List<T>> tuple) {
        FlowEventAckDelivery<S> delivery = tuple.getT1();
        List<T> events = tuple.getT2();
        return sender.sendWithPublishConfirms(routerSpecification.getOutputExchange(), Flux.fromIterable(events))
                .doOnNext(publishConfirmResult -> {
                    if (publishConfirmResult.allSent()) {
                        retryAcknowledgement.ack(delivery);
                    } else {
                        retryAcknowledgement.nack(delivery, false);
                    }
                }).map(result -> tuple);
    }

    private BindingSpecification createFrom(RouterSpecification routerSpecification) {
        return BindingSpecification.binding()
                .queue(routerSpecification.getIntermediateQueue())
                .routingKey(routerSpecification.getRoutingKey())
                .exchange(routerSpecification.getInputExchange());
    }


}
