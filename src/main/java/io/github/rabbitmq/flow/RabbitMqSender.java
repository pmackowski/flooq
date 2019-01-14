package io.github.rabbitmq.flow;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.Sender;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

class RabbitMqSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqSender.class);

    private static final Gson gson = new Gson();

    private Sender sender;
    private RabbitMqDeclareOptions rabbitMqDeclareOptions;

    RabbitMqSender(Sender sender, RabbitMqDeclareOptions rabbitMqDeclareOptions) {
        this.sender = sender;
        this.rabbitMqDeclareOptions = rabbitMqDeclareOptions;
    }

    Mono<Void> send(String exchangeName, Publisher<? extends FlowEvent> flowEvents, Type type) {
        return send(exchangeName, flowEvents, flowEvent -> gson.toJson(flowEvent, type).getBytes());
    }

    Mono<Void> send(String exchangeName, Publisher<? extends FlowEvent> flowEvents) {
        return send(exchangeName, flowEvents, flowEvent -> gson.toJson(flowEvent).getBytes());
    }

    private Mono<Void> send(String exchangeName, Publisher<? extends FlowEvent> flowEvents, Function<FlowEvent,byte[]> toMessageBodyFunc) {
        return sender.send(messages(exchangeName, flowEvents, toMessageBodyFunc));
    }

    public Mono<PublishConfirmResult> sendWithPublishConfirms(String exchangeName, Publisher<? extends FlowEvent> events) {
        return sender.sendWithPublishConfirms(messages(exchangeName, events, flowEvent -> gson.toJson(flowEvent).getBytes()))
                .doOnNext(result -> LOGGER.debug("Following event has been sent to exchange {}: {}", exchangeName, messageBody(result)))
                // does not scale for long or infinite events
                .buffer()
                .map(outboundMessageResults -> new PublishConfirmResult(outboundMessageResults.size(), outboundMessageResults))
                .next()
                .doOnNext(publishConfirmResult -> LOGGER.debug("Publish confirm result {}", publishConfirmResult))
                .onErrorReturn(PublishConfirmResult.FALLBACK_VALUE);
    }

    private Flux<OutboundMessage> messages(String exchangeName, Publisher<? extends FlowEvent> flowEvents, Function<FlowEvent,byte[]> toMessageBodyFunc) {
        return Flux.from(flowEvents).map(flowEvent -> message(exchangeName, flowEvent, toMessageBodyFunc.apply(flowEvent)));
    }

    private OutboundMessage message(String exchangeName, FlowEvent flowEvent, byte[] messageBody) {
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .deliveryMode(rabbitMqDeclareOptions.getMessageDeliveryMode())
                .priority(flowEvent.getPriority())
                .build();
        return new OutboundMessage(exchangeName, flowEvent.getRoutingKey(), basicProperties, messageBody);
    }

    private String messageBody(OutboundMessageResult result) {
        return new String(result.getOutboundMessage().getBody());
    }
}
