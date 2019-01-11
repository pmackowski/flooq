package io.github.rabbitmq.flow;

public enum ExchangeType {

    TOPIC("topic"),
    CONSISTENT_HASH("x-consistent-hash");

    private String value;

    ExchangeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
