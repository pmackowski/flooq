package io.github.rabbitmq.flow;

import reactor.rabbitmq.AcknowledgableDelivery;

public class FlowEventAckDelivery<T extends FlowEvent> {

    private T message;
    private AcknowledgableDelivery delivery;

    public FlowEventAckDelivery(T message, AcknowledgableDelivery delivery) {
        this.message = message;
        this.delivery = delivery;
    }

    public T getMessage() {
        return message;
    }

    public AcknowledgableDelivery getDelivery() {
        return delivery;
    }
}
