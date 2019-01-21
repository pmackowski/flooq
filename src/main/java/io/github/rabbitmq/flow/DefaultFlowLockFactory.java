package io.github.rabbitmq.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// temporary to check how it could work
public class DefaultFlowLockFactory implements FlowLockFactory {

    private Map<String, FlowLock> locks = new HashMap<>();
    private Lock lock = new ReentrantLock();

    @Override
    public FlowLock getLock(String name) {
        lock.lock();
        try {
            if (locks.containsKey(name)) {
                return locks.get(name);
            }
            Lock lock = new ReentrantLock();
            FlowLock flowLock = new FlowLock() {
                @Override
                public boolean tryLock() {
                    return lock.tryLock();
                }

                @Override
                public boolean isLockedByCurrentThread() {
                    return ((ReentrantLock) lock).isHeldByCurrentThread();
                }

                @Override
                public void unlock() {
                    lock.unlock();
                }
            };
            locks.put(name, flowLock);
            return flowLock;
        } finally {
             lock.unlock();
        }
    }
}
