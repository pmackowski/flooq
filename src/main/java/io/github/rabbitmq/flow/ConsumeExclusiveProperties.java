package io.github.rabbitmq.flow;

import java.time.Duration;

public class ConsumeExclusiveProperties {

    private int qos = 250;
    private Duration stopInterval = Duration.ofHours(3);

    public ConsumeExclusiveProperties qos(int qos) {
        this.qos = qos;
        return this;
    }

    public ConsumeExclusiveProperties stopInterval(Duration stopInterval) {
        this.stopInterval = stopInterval;
        return this;
    }

    public int getQos() {
        return qos;
    }

    public Duration getStopInterval() {
        return stopInterval;
    }
}
