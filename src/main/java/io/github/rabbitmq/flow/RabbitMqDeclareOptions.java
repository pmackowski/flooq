package io.github.rabbitmq.flow;

class RabbitMqDeclareOptions {

    private int messageDeliveryMode = 1;
    private boolean durable = false;

    public int getMessageDeliveryMode() {
        return messageDeliveryMode;
    }

    public RabbitMqDeclareOptions persistentMessages(boolean persistentMessages) {
        this.messageDeliveryMode = persistentMessages ? 2 : 1;
        return this;
    }

    public boolean isDurable() {
        return durable;
    }

    public RabbitMqDeclareOptions durable(boolean durable) {
        this.durable = durable;
        return this;
    }
}
