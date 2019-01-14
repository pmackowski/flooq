package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.rabbitmq.ConsumeOptions;

public class ConsumeProperties {

    public static final String CONSUMER_STOP_COMMAND = "#stop#";

    private int qos = 250;
    private boolean retry = true;
    private boolean repeatable = true;

    ConsumeOptions toOptions() {
        return new ConsumeOptions()
                .stopConsumingBiFunction((aLong, delivery) -> command(delivery))
                .qos(qos);
    }

    public ConsumeProperties qos(int qos) {
        this.qos = qos;
        return this;
    }

    public ConsumeProperties retry(boolean retry) {
        this.retry = retry;
        return this;
    }

    public ConsumeProperties repeatable(boolean repeatable) {
        this.repeatable = repeatable;
        return this;
    }

    boolean command(Delivery delivery) {
        return CONSUMER_STOP_COMMAND.equals(new String(delivery.getBody()));
    }

    boolean notCommand(Delivery delivery) {
        return !command(delivery);
    }

}
