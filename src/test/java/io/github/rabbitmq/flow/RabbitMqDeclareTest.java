package io.github.rabbitmq.flow;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import io.github.rabbitmq.flow.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.*;
import reactor.test.StepVerifier;

import java.io.IOException;

import static io.github.rabbitmq.flow.RabbitMqDeclare.DEAD_LETTER_EXCHANGE;
import static io.github.rabbitmq.flow.RabbitMqDeclare.DEAD_LETTER_SUFFIX;
import static io.github.rabbitmq.flow.utils.TestUtils.consume;
import static io.github.rabbitmq.flow.utils.TestUtils.outboundMessageFlux;

class RabbitMqDeclareTest {

    static final String EXCHANGE_NAME = "flow.topic";
    static final String QUEUE_NAME = "flow";
    static final String ROUTING_KEY = "flow.*";
    static final String MESSAGE_ROUTING_KEY = "flow.abc";

    Connection connection;
    Sender sender;
    RabbitMqDeclare rabbitMqDeclare;

    @BeforeEach
    public void init() throws Exception {
        connection = TestUtils.newConnection();
        sender = RabbitFlux.createSender();
        rabbitMqDeclare = new RabbitMqDeclare(sender);
    }

    @AfterEach
    public void tearDown() throws IOException {
        sender.close();
        connection.close();
    }

    @Test
    void declareAll() {
        int nbMessages = 10;

        BindingSpecification bindingSpecification = new BindingSpecification()
                .exchange(EXCHANGE_NAME)
                .queue(QUEUE_NAME)
                .routingKey(ROUTING_KEY);

        Flux<Delivery> publisher = rabbitMqDeclare.declareAll(bindingSpecification)
                .then(sender.send(outboundMessageFlux(EXCHANGE_NAME, MESSAGE_ROUTING_KEY, nbMessages)))
                .thenMany(consume(connection, QUEUE_NAME, nbMessages));

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNextCount(nbMessages)
            .verifyComplete();

        sender.delete(new ExchangeSpecification().name(EXCHANGE_NAME)).block();
        sender.delete(new ExchangeSpecification().name(DEAD_LETTER_EXCHANGE)).block();
        sender.delete(new QueueSpecification().name(QUEUE_NAME)).block();
        sender.delete(new QueueSpecification().name(QUEUE_NAME + DEAD_LETTER_SUFFIX)).block();

    }
}