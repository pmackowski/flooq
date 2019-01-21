package io.github.rabbitmq.flow;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

public class FlowOptions {

    private FlowLock flowLock;

    private String name;

    private Sender sender;

    private Receiver receiver;

    public FlowOptions() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        // one connection per whole flow (for testing purposes)!!! important especially for exclusive queues
        Mono<? extends Connection> connectionMono = Utils.singleConnectionMono(connectionFactory);
        this.sender = RabbitFlux.createSender(new SenderOptions().connectionMono(connectionMono));
        this.receiver = RabbitFlux.createReceiver(new ReceiverOptions().connectionMono(connectionMono));
    }

    public FlowLock getFlowLock() {
        return flowLock;
    }

    public FlowOptions flowLock(FlowLock flowLock) {
        this.flowLock = flowLock;
        return this;
    }

    public Sender getSender() {
        return sender;
    }

    public FlowOptions sender(Sender sender) {
        this.sender = sender;
        return this;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public FlowOptions setReceiver(Receiver receiver) {
        this.receiver = receiver;
        return this;
    }
}
