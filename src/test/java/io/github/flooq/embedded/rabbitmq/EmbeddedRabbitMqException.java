package io.github.flooq.embedded.rabbitmq;

public class EmbeddedRabbitMqException extends RuntimeException {

    public EmbeddedRabbitMqException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
