package io.github.rabbitmq.flow;

public enum ExchangeType {

    TOPIC("topic", "topic"),
    CONSISTENT_HASH("x-consistent-hash", "topic.partition");

    private String value;
    private String suffix;

    ExchangeType(String value, String suffix) {
        this.value = value;
        this.suffix = suffix;
    }

    public String getValue() {
        return value;
    }

    public String getSuffix() {
        return suffix;
    }
}
