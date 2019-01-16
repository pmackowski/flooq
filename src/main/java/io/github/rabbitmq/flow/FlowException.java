package io.github.rabbitmq.flow;

public class FlowException extends RuntimeException {

    public FlowException(Throwable throwable) {
        super(throwable);
    }
}
