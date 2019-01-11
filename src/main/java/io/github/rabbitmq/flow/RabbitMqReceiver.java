package io.github.rabbitmq.flow;

import com.google.gson.Gson;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

public class RabbitMqReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqReceiver.class);

    private static final Gson gson = new Gson();

    private Receiver receiver;
    private RabbitMqDeclare rabbitMqDeclare;

    public RabbitMqReceiver(Receiver receiver, RabbitMqDeclare rabbitMqDeclare) {
        this.receiver = receiver;
        this.rabbitMqDeclare = rabbitMqDeclare;
    }

    public <T extends FlowEvent> Flux<FlowEventDelivery<T>> consumeAutoAck(BindingSpecification bindingSpecification, Class<T> type) {
        String queue = getQueue(bindingSpecification);
        Flux<Delivery> messages = receiver.consumeAutoAck(queue);

        return rabbitMqDeclare.declareAll(bindingSpecification)
                .thenMany(messages)
                .flatMapSequential(delivery ->
                        Flux.just(delivery)
                                .map(delivery1 -> new FlowEventDelivery<>(message(delivery1, type), delivery1))
                                .doOnError(throwable -> LOGGER.info("dupa", throwable))
                                .onErrorResume(e -> Flux.empty())
                );
    }

    private String getQueue(BindingSpecification bindingSpecification) {
        return bindingSpecification.getQueue() == null ? bindingSpecification.getExchange() : bindingSpecification.getQueue();
    }

    public <T> T message(Delivery delivery, Class<T> type) {
        byte[] body = delivery.getBody();
        return gson.fromJson(new String(body), type);
    }

}
