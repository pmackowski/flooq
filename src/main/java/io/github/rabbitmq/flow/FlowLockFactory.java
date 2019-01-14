package io.github.rabbitmq.flow;

public interface FlowLockFactory {

    FlowLock getLock(String name);

}
