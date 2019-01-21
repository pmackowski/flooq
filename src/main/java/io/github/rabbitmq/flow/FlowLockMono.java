package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

class FlowLockMono {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowLockMono.class);

    private final FlowLockFactory flowLockFactory;

    public FlowLockMono() {
        this.flowLockFactory = new DefaultFlowLockFactory();
    }

    public FlowLockMono(FlowLockFactory flowLockFactory) {
        this.flowLockFactory = flowLockFactory;
    }

    public Mono<Void> tryLock(String name, Publisher<?> publisher) {
        return tryLock(name, publisher,
                "Process acquired lock for resource {}",
                "Process released lock for resource {} due to signal type {}");
    }

    private Mono<Void> tryLock(String name, Publisher<?> publisher, String lockAcquireMessage, String lockReleaseMessage) {
        Scheduler scheduler = Schedulers.newSingle(name.toLowerCase());

        return Mono.defer(() -> Mono.just(flowLockFactory.getLock(name)))
                .publishOn(scheduler)
                .filter(FlowLock::tryLock)
                .doOnNext(lock -> LOGGER.info(lockAcquireMessage, name))
                .flatMapMany(lock -> Flux.from(publisher).subscribeOn(scheduler))
                .publishOn(scheduler)
                .doFinally(signalType -> {
                    FlowLock lock = flowLockFactory.getLock(name);
                    if (signalType == SignalType.CANCEL) {
                        scheduler.schedule(() -> {
                            lock.unlock();
                            LOGGER.info(lockReleaseMessage, name, signalType);
                        });
                    } else if (lock.isLockedByCurrentThread()) {
                        lock.unlock();
                        LOGGER.info(lockReleaseMessage, name, signalType);
                    }
                })
                .then();
    }


}
