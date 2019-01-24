package io.github.flooq;

public interface FlooqLock {

    boolean tryLock();

    boolean isLockedByCurrentThread();

    void unlock();
}
