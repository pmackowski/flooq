package io.github.rabbitmq.flow;

import com.google.gson.Gson;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.Receiver;

public class RabbitMqReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqReceiver.class);

    private static final String QUEUE_CANNOT_BE_USED_RETRYING = "Queue {} cannot be used. Retrying...";

    private static final Gson gson = new Gson();

    private Receiver receiver;
    private RabbitMqDeclare rabbitMqDeclare;
    private RetryAcknowledgement retryAcknowledgement;

    public RabbitMqReceiver(Receiver receiver, RabbitMqDeclare rabbitMqDeclare, RetryAcknowledgement retryAcknowledgement) {
        this.receiver = receiver;
        this.rabbitMqDeclare = rabbitMqDeclare;
        this.retryAcknowledgement = retryAcknowledgement;
    }

    public <T extends FlowEvent> Flux<FlowEventDelivery<T>> consumeAutoAck(BindingSpecification bindingSpecification, Class<T> type) {
        return consumeAutoAck(bindingSpecification, type, new ConsumeProperties());
    }

    public <T extends FlowEvent> Flux<FlowEventDelivery<T>> consumeAutoAck(BindingSpecification bindingSpecification, Class<T> type, ConsumeProperties consumeProperties) {
        String queue = getQueue(bindingSpecification);
        Flux<Delivery> messages = receiver.consumeAutoAck(queue, consumeProperties.toOptions())
                .filter(consumeProperties::notCommand)
                .doOnError(e -> LOGGER.error(QUEUE_CANNOT_BE_USED_RETRYING, queue, e));
                // TODO repeat when consumeProperties.isRepeatable()
                // TODO retry when consumeProperties.isRetry()

        return rabbitMqDeclare.declareAll(bindingSpecification)
                .thenMany(messages)
                .flatMapSequential(delivery ->
                        Flux.just(delivery)
                                .map(delivery1 -> new FlowEventDelivery<>(message(delivery1, type), delivery1))
                                .doOnNext(deliveryWrapper -> LOGGER.debug("Consuming from queue {} message {}", queue, deliveryWrapper.getMessage()))
                                .doOnError(e -> LOGGER.error("Message in queue {} cannot be processed due to the wrong format. It will be ignored! Message body is\n {}", queue, new String(delivery.getBody()), e))
                                .onErrorResume(e -> Flux.empty())
                );
    }

    public <T extends FlowEvent> Flux<FlowEventAckDelivery<T>> consumeManualAck(BindingSpecification bindingSpecification, Class<T> type) {
        return consumeManualAck(bindingSpecification, type, new ConsumeProperties());
    }

    public <T extends FlowEvent> Flux<FlowEventAckDelivery<T>> consumeManualAck(BindingSpecification bindingSpecification, Class<T> type, ConsumeProperties consumeProperties) {
        String queue = getQueue(bindingSpecification);
        Flux<AcknowledgableDelivery> messages = receiver.consumeManualAck(queue, consumeProperties.toOptions())
                .doOnNext(delivery -> {
                    if (consumeProperties.command(delivery)) {
                        retryAcknowledgement.ack(delivery);
                    }
                })
                .filter(consumeProperties::notCommand)
                .doOnError(e -> LOGGER.error(QUEUE_CANNOT_BE_USED_RETRYING, queue, e));
                // TODO repeat when consumeProperties.isRepeatable()
                // TODO retry when consumeProperties.isRetry()

        return rabbitMqDeclare.declareAll(bindingSpecification)
                .thenMany(messages)
                .flatMapSequential(delivery ->
                        Mono.just(delivery)
                                .map(delivery1 -> new FlowEventAckDelivery<>(message(delivery1, type), delivery1))
                                .doOnNext(deliveryWrapper -> LOGGER.debug("Consuming from queue {} message {}", queue, deliveryWrapper.getMessage()))
                                .doOnError(e -> {
                                    LOGGER.error("Message in queue {} cannot be processed due to the wrong format. It will be moved to dead letter queue! Message body is\n {}", queue, new String(delivery.getBody()), e);
                                    retryAcknowledgement.nack(delivery, false);
                                })
                                .onErrorResume(e -> Mono.empty()));
    }

    private String getQueue(BindingSpecification bindingSpecification) {
        return bindingSpecification.getQueue() == null ? bindingSpecification.getExchange() : bindingSpecification.getQueue();
    }

    public <T> T message(Delivery delivery, Class<T> type) {
        byte[] body = delivery.getBody();
        return gson.fromJson(new String(body), type);
    }

}
