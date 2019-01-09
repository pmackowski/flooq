package io.github.rabbitmq.flow;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.lang.reflect.Type;
import java.util.function.Function;

class RabbitMqSender {

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

}
