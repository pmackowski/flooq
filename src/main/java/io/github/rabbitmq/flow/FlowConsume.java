package io.github.rabbitmq.flow;

import reactor.core.publisher.Flux;

import java.util.function.Function;

public class FlowConsume<T extends FlowEvent> {

    private String inputExchange;
    private String routingKey;
    private String queueName;
    private int innerQueues = 1;
    private Class<T> type;
    private Function<Flux<T>,Flux<T>> func;

    public FlowConsume<T> inputExchange(String inputExchange) {
        this.inputExchange = inputExchange;
        return this;
    }

    public FlowConsume<T> routingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    public FlowConsume<T> queueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public FlowConsume<T> type(Class<T> type) {
        this.type = type;
        return this;
    }

    public FlowConsume<T> virtualQueue(String queueName, int innerQueues) {
        this.queueName = queueName;
        this.innerQueues = innerQueues;
        return this;
    }

    public FlowConsume<T> consume(Function<Flux<T>,Flux<T>> func) {
        this.func = func;
        return this;
    }

    public String getInputExchange() {
        return inputExchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getQueueName() {
        return queueName;
    }

    public Class<T> getType() {
        return type;
    }

    public Function<Flux<T>, Flux<T>> getFunc() {
        return func;
    }

    public int getInnerQueues() {
        return innerQueues;
    }
}
