package io.github.flooq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

public class FlooqOptions {

    private FlooqLock flooqLock;

    private String name;

    private Sender sender;

    private Receiver receiver;

    public FlooqOptions() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        // one connection per whole flow (for testing purposes)!!! important especially for exclusive queues
        Mono<? extends Connection> connectionMono = Utils.singleConnectionMono(connectionFactory);
        this.sender = RabbitFlux.createSender(new SenderOptions().connectionMono(connectionMono));
        this.receiver = RabbitFlux.createReceiver(new ReceiverOptions().connectionMono(connectionMono));
    }

    public FlooqLock getFlooqLock() {
        return flooqLock;
    }

    public FlooqOptions flooqLock(FlooqLock flooqLock) {
        this.flooqLock = flooqLock;
        return this;
    }

    public Sender getSender() {
        return sender;
    }

    public FlooqOptions sender(Sender sender) {
        this.sender = sender;
        return this;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public FlooqOptions setReceiver(Receiver receiver) {
        this.receiver = receiver;
        return this;
    }
}
