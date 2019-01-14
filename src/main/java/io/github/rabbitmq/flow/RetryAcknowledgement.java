package io.github.rabbitmq.flow;

import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.ExceptionHandlers;

public class RetryAcknowledgement {

    private final ExceptionHandlers.SimpleRetryTemplate retryTemplate;

    public RetryAcknowledgement(ExceptionHandlers.SimpleRetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public void ack(FlowEventAckDelivery flowEventAckDelivery) {
        ack(flowEventAckDelivery.getDelivery());
    }

    public void ack(AcknowledgableDelivery acknowledgableDelivery) {
        try {
            acknowledgableDelivery.ack();
        } catch (Exception e) {
            this.retryTemplate.retry(() -> {
                acknowledgableDelivery.ack();
                return null;
            }, e);
        }
    }

    public void nack(FlowEventAckDelivery flowEventAckDelivery, boolean requeue) {
        nack(flowEventAckDelivery.getDelivery(), requeue);
    }

    public void nack(AcknowledgableDelivery delivery, boolean requeue) {
        try {
            delivery.nack(requeue);
        } catch (Exception e) {
            this.retryTemplate.retry(() -> {
                delivery.nack(requeue);
                return null;
            }, e);
        }
    }
}
