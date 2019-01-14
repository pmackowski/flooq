package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;

public class FlowEventDelivery<T extends FlowEvent> {

    private T message;
    private Delivery delivery;

    public FlowEventDelivery(T message, Delivery delivery) {
        this.message = message;
        this.delivery = delivery;
    }

    public T getMessage() {
        return message;
    }

    public Delivery getDelivery() {
        return delivery;
    }

}
