package io.github.rabbitmq.flow;

import com.rabbitmq.client.Connection;
import reactor.core.publisher.Mono;

public class FlowOptions {

    private Mono<? extends Connection> connectionMono;

    private FlowLock flowLock;

}
