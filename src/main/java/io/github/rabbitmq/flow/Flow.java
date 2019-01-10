package io.github.rabbitmq.flow;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class Flow<T> {

    public Flow<T> begin(Function<FlowBegin<T>, FlowBegin<T>> flowBegin) {
        return new Flow<>();
    }

    public Flow<T> route(Function<FlowRoute, FlowRoute> flowRoute) {
        return this;
    }

    public Flow<T> consume(Function<FlowConsume, FlowConsume> flowConsume) {
        return this;
    }

    public Flux<T> end(Function<FlowConsume, FlowConsume> flowConsume) {
        return Flux.empty();
    }

    public Mono<Void> end() {
        return Mono.empty();
    }

}
