package io.github.rabbitmq.flow;

public interface FlowLock {

    boolean tryLock();

    boolean isLockedByCurrentThread();

    void unlock();
}
