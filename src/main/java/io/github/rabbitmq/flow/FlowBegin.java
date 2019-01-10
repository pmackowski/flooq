package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;

public class FlowBegin<T> {

    public FlowBegin<T> exchangeName(String exchangeName) {
        return this;
    }

    public FlowBegin<T> publisher(Publisher<T> publisher) {
        return this;
    }


}
