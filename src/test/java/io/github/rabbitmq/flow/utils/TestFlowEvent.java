package io.github.rabbitmq.flow.utils;

import com.google.gson.Gson;
import com.rabbitmq.client.Delivery;
import io.github.rabbitmq.flow.FlowEvent;

import java.util.Objects;

public class TestFlowEvent implements FlowEvent {

    private static Gson gson = new Gson();

    private final int i;
    private final String routingKey;

    private TestFlowEvent(int i, String routingKey) {
        this.i = i;
        this.routingKey = routingKey;
    }

    public static TestFlowEvent create(Delivery delivery) {
        return gson.fromJson(new String(delivery.getBody()), TestFlowEvent.class);
    }

    public static TestFlowEvent create(int i, String routingKey) {
        return new TestFlowEvent(i, routingKey);
    }

    public int getI() {
        return i;
    }

    @Override
    public String getRoutingKey() {
        return routingKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestFlowEvent that = (TestFlowEvent) o;
        return i == that.i &&
                Objects.equals(routingKey, that.routingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, routingKey);
    }

    @Override
    public String toString() {
        return "TestFlowEvent{" +
                "i=" + i +
                ", routingKey='" + routingKey + '\'' +
                '}';
    }
}
