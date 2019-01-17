package io.github.rabbitmq.flow.utils;

import com.rabbitmq.client.*;
import io.github.rabbitmq.flow.FlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    public static Connection newConnection() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        return connectionFactory.newConnection();
    }

    public static Flux<OutboundMessage> outboundMessageFlux(String queue, int nbMessages) {
        return Flux.range(0, nbMessages).map(i -> new OutboundMessage("", queue, "".getBytes()));
    }

    public static Flux<OutboundMessage> outboundMessageFlux(String exchange, String routingKey, int nbMessages) {
        return Flux.range(0, nbMessages).map(i -> new OutboundMessage(exchange, routingKey, "".getBytes()));
    }

    public static Flux<OutboundMessage> outboundMessageFlux(String exchange, Supplier<String> routingKey, int nbMessages) {
        return Flux.range(0, nbMessages)
                    .map(i -> new OutboundMessage(exchange, routingKey.get(), String.valueOf(i).getBytes()))
                    .doOnNext(s -> LOGGER.info("Send {}", new String(s.getBody())));
    }

    public static String declareQueue(Connection connection) throws Exception {
        String queueName = UUID.randomUUID().toString();
        Channel channel = connection.createChannel();
        String queue = channel.queueDeclare(queueName, false, false, false, null).getQueue();
        channel.close();
        return queue;
    }

    public static void deleteQueue(Connection connection, String queue) throws Exception {
        Channel channel = connection.createChannel();
        channel.queueDelete(queue);
        channel.close();
    }

    public static Flux<Delivery> consume(Connection connection, final String queue, int nbMessages) {
        return consume(connection, queue, nbMessages, Duration.ofSeconds(1));
    }

    public static Flux<Delivery> consume(Connection connection, final String queue, int nbMessages, Duration timeout) {
        Channel channel = createChannel(connection);
        Flux<Delivery> consumeFlux = Flux.create(emitter -> Mono.just(nbMessages).map(AtomicInteger::new).subscribe(countdown -> {
            DeliverCallback deliverCallback = (consumerTag, message) -> {
                emitter.next(message);
                if (countdown.decrementAndGet() <= 0) {
                    emitter.complete();
                }
            };
            try {
                channel.basicConsume(queue, true, deliverCallback, consumerTag -> {});
            } catch (IOException e) {
                throw new FlowException(e);
            }
        }));
        return consumeFlux.timeout(timeout);
    }

    public static Channel createChannel(Connection connection) {
        try {
            return connection.createChannel();
        } catch (Exception e) {
            throw new FlowException(e);
        }
    }

}
