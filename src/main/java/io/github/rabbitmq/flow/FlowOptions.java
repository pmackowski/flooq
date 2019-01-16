package io.github.rabbitmq.flow;

import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

public class FlowOptions {

    private FlowLock flowLock;

    private Sender sender = RabbitFlux.createSender();

    private Receiver receiver = RabbitFlux.createReceiver();

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
