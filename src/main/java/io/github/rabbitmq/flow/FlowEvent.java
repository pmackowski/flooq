package io.github.rabbitmq.flow;

public interface FlowEvent {

    String NO_ROUTING_KEY = "";

    default String getRoutingKey() {
        return NO_ROUTING_KEY;
    }

    default int getPriority() {
        return 0;
    }

}
