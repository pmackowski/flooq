package io.github.rabbitmq.flow;

import reactor.core.publisher.Mono;

public interface Flow {

    Mono<Void> start();

}
