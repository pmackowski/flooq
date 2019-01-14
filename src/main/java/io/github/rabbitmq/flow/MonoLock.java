package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class MonoLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonoLock.class);

    private final FlowLockFactory flowLockFactory;

    public MonoLock(FlowLockFactory flowLockFactory) {
        this.flowLockFactory = flowLockFactory;
    }

    public Mono<FlowLock> monoLock(String name) {
        return Mono.defer(() -> Mono.just(flowLockFactory.getLock(name)));
    }

    public Mono<Void> tryLock(String name, Publisher<?> publisher) {
        return tryLock(name, publisher, "Server acquired lock {}", "Server released lock {}");
    }

    public Mono<Void> tryLockForQueue(String name, Publisher<?> publisher) {
        return tryLock(name, publisher,
                "Server acquired lock for queue {} for exclusive usage",
                "Server released lock for queue {}");
    }

    private Mono<Void> tryLock(String name, Publisher<?> publisher, String lockAcquireMessage, String lockReleaseMessage) {
        Scheduler scheduler = Schedulers.newSingle(name.toLowerCase());

        return Mono.defer(() -> Mono.just(flowLockFactory.getLock(name)))
                .publishOn(scheduler)
                .filter(FlowLock::tryLock)
                .doOnNext(lock -> LOGGER.debug(lockAcquireMessage, name))
                .flatMapMany(lock -> publisher)
                .publishOn(scheduler)
                .doFinally(signalType -> {
                    FlowLock lock = flowLockFactory.getLock(name);
                    if (lock.isLockedByCurrentThread()) {
                        lock.unlock();
                        LOGGER.debug(lockReleaseMessage, name);
                    }
                })
                .then();
    }

}
