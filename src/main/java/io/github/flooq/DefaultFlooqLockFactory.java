package io.github.flooq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// temporary to check how it could work
public class DefaultFlooqLockFactory implements FlooqLockFactory {

    private Map<String, FlooqLock> locks = new HashMap<>();
    private Lock lock = new ReentrantLock();

    @Override
    public FlooqLock getLock(String name) {
        lock.lock();
        try {
            if (locks.containsKey(name)) {
                return locks.get(name);
            }
            Lock lock = new ReentrantLock();
            FlooqLock flooqLock = new FlooqLock() {
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
            locks.put(name, flooqLock);
            return flooqLock;
        } finally {
             lock.unlock();
        }
    }
}
