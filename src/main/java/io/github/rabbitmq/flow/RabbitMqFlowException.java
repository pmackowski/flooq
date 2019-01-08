package io.github.rabbitmq.flow;

public class RabbitMqFlowException extends RuntimeException {

    public RabbitMqFlowException(Throwable cause) {
        super(cause);
    }

    public RabbitMqFlowException(String message) {
        super(message);
    }

    public RabbitMqFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
