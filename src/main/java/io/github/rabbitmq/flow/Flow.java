package io.github.rabbitmq.flow;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExceptionHandlers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Flow<T extends FlowEvent> {

    private final RabbitMqSender rabbitMqSender;
    private final RabbitMqDeclare rabbitMqDeclare;
    private final RabbitMqReceiver rabbitMqReceiver;

    private FlowBegin<T> flowBegin = new FlowBegin<>();
    private FlowConsume<T> flowConsume = new FlowConsume<>();

    public Flow() {
        Sender sender = RabbitFlux.createSender();
        this.rabbitMqSender = new RabbitMqSender(sender, new RabbitMqDeclareOptions());
        this.rabbitMqDeclare = new RabbitMqDeclare(sender);
        this.rabbitMqReceiver = new RabbitMqReceiver(RabbitFlux.createReceiver(), rabbitMqDeclare,
                new RetryAcknowledgement(new ExceptionHandlers.SimpleRetryTemplate(Duration.ofSeconds(20), Duration.ofMillis(500),
                        ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE)));
    }

    public Flow<T> begin(Function<FlowBegin<T>, FlowBegin<T>> flowBeginFunction) {
        this.flowBegin = flowBeginFunction.apply(new FlowBegin<>());
        return this;
    }

    public Flow<T> route(Function<FlowRoute, FlowRoute> flowRoute) {
        return this;
    }

    public Flow<T> consume(Function<FlowConsume, FlowConsume> flowConsume) {
        return this;
    }

    public Flux<T> end(Function<FlowConsume<T>, FlowConsume<T>> flowConsumeFunction) {
        this.flowConsume = flowConsumeFunction.apply(new FlowConsume<>());

        if (flowConsume.getInnerQueues() == 1) {

            BindingSpecification bindingSpecification = new BindingSpecification()
                    .queue(flowConsume.getQueueName())
                    .exchange(flowBegin.getExchangeName())
                    .routingKey(flowConsume.getRoutingKey());

            return rabbitMqDeclare.declareAll(bindingSpecification)
                    .then(rabbitMqSender.send(flowBegin.getExchangeName(), flowBegin.getPublisher()))
                    .thenMany(rabbitMqReceiver.consumeAutoAck(bindingSpecification, flowConsume.getType())
                            .map(FlowEventDelivery::getMessage)
                            .transform(flowConsume.getFunc())
                    );
        } else {
            List<Flux<T>> fluxes = new ArrayList<>(flowConsume.getInnerQueues());
            for (int i = 0; i < flowConsume.getInnerQueues(); i++) {
                BindingSpecification bindingSpecification = new BindingSpecification()
                        .queue(flowConsume.getQueueName() + "." + (i+1))
                        .routingKey(String.valueOf(i+1))
                        .exchange(flowBegin.getExchangeName());
                fluxes.add(rabbitMqDeclare.declareAll(bindingSpecification)
                        //.then(rabbitMqSender.send(flowBegin.getExchangeName(), flowBegin.getPublisher()))
                        .thenMany(rabbitMqReceiver.consumeAutoAck(bindingSpecification, flowConsume.getType())
                                .map(FlowEventDelivery::getMessage)
                                .transform(flowConsume.getFunc())
                        ));
            }
            return Flux.merge(fluxes);
        }

    }

    public Mono<Void> end() {
        return rabbitMqDeclare.declareExchange(flowBegin.getExchangeName(), flowBegin.getExchangeType())
                              .then(rabbitMqSender.send(flowBegin.getExchangeName(), flowBegin.getPublisher()))
        .then();
    }

}
