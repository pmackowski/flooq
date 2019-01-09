package io.github.rabbitmq.flow;

import com.rabbitmq.client.Connection;
import io.github.rabbitmq.flow.utils.TestFlowEvent;
import io.github.rabbitmq.flow.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.test.StepVerifier;

import static io.github.rabbitmq.flow.utils.TestUtils.consume;
import static io.github.rabbitmq.flow.utils.TestUtils.flowEventFlux;

public class RabbitMqSenderTest {

    static final String EXCHANGE_NAME = "";

    Connection connection;
    Sender sender;
    RabbitMqSender rabbitMqSender;
    String queue;

    @BeforeEach
    public void init() throws Exception {
        connection = TestUtils.newConnection();
        sender = RabbitFlux.createSender();
        queue = TestUtils.declareQueue(connection);
        rabbitMqSender = new RabbitMqSender(sender, new RabbitMqDeclareOptions());
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.deleteQueue(connection, queue);
        sender.close();
        connection.close();
    }

    @Test
    void send() {
        int nbMessages = 2;

        Publisher<TestFlowEvent> publisher = rabbitMqSender.send(EXCHANGE_NAME, flowEventFlux(queue, nbMessages))
                .thenMany(consume(connection, queue, nbMessages))
                .map(TestFlowEvent::create);

        StepVerifier.create(publisher)
                .expectSubscription()
                .expectNext(TestFlowEvent.create(0, queue))
                .expectNext(TestFlowEvent.create(1, queue))
                .verifyComplete();
    }

}
