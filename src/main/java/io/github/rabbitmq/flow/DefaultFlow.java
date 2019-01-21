package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import io.github.rabbitmq.flow.utils.RepeatUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.ConsumeOptions;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.github.rabbitmq.flow.utils.RetryUtils.defaultExponentialBackoff;

class DefaultFlow implements Flow {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFlow.class);

    public static final String CONSUMER_STOP_COMMAND = "#stop#";

    private final FlowOptions flowOptions;
    private final AmqpEntities amqpEntities;
    private final FlowLockMono flowLockMono;

    DefaultFlow(FlowOptions flowOptions, AmqpEntities amqpEntities) {
        this.flowOptions = flowOptions;
        this.amqpEntities = amqpEntities;
        this.flowLockMono = new FlowLockMono();
    }

    @Override
    public Disposable start() {
        String uuid = UUID.randomUUID().toString();

        Sender sender = flowOptions.getSender();

        Receiver receiver = flowOptions.getReceiver();

        List<Publisher<?>> publishers = new ArrayList<>();

        ConsumeOptions consumeOptions =  new ConsumeOptions()
                .stopConsumingBiFunction((aLong, delivery) -> command(delivery));

        List<Publisher<?>> consumers = amqpEntities.getConsumerSpecifications().stream().map(
                consumerSpecification -> {
                    Flux<? extends Delivery> transform;
                    if (consumerSpecification.getConsumeNoAck() != null) {
                        transform = receiver.consumeNoAck(consumerSpecification.getQueue(), consumeOptions)
                                .doOnNext(s -> LOGGER.info(uuid + "Consuming on quqeue {} {}", consumerSpecification.getQueue(), new String(s.getBody())))
                                .transform(consumerSpecification.getConsumeNoAck());
                    } else if (consumerSpecification.getConsumeAutoAck() != null) {
                        transform = receiver.consumeAutoAck(consumerSpecification.getQueue(), consumeOptions)
                                .doOnNext(s -> LOGGER.info(uuid + "Consuming on quqeue {} {}", consumerSpecification.getQueue(), new String(s.getBody())))
                                .transform(consumerSpecification.getConsumeAutoAck());
                    } else {
                        transform = receiver.consumeManualAck(consumerSpecification.getQueue(), consumeOptions)
                                .doOnNext(s -> LOGGER.info(uuid + ": Consuming on quqeue {} {}", consumerSpecification.getQueue(), new String(s.getBody())))
                                .transform(consumerSpecification.getConsumeManualAck());
                    }

                    if (consumerSpecification.getExchange() != null) {
                        return transform.flatMap(delivery -> sender.send(Mono.just(
                                new OutboundMessage(consumerSpecification.getExchange(), delivery.getEnvelope().getRoutingKey(), delivery.getBody())
                        )));
                    }

                    if (consumerSpecification.isAtMostOne() ) {
                        String tryObtainLockMessage = uuid + ": Trying to acquire lock for queue " + consumerSpecification.getQueue();
                        return flowLockMono.tryLock(consumerSpecification.getQueue(), transform)
                                .repeatWhen(RepeatUtils.afterOneSecond(tryObtainLockMessage))
                                .retryWhen(defaultExponentialBackoff(tryObtainLockMessage));
                    } else {
                        String message = "No lock. Just repeating " + consumerSpecification.getQueue();
                        return transform.repeatWhen(RepeatUtils.afterOneSecond(message));
                    }
                }
        ).collect(Collectors.toList());

        publishers.addAll(consumers);
        publishers.addAll(amqpEntities.getPublishers().stream().map(
                publisher -> {
                    String tryObtainLockMessage = uuid + ": Trying to acquire lock for publisher ";
                    return flowLockMono.tryLock("myPubliser", sender.sendWithPublishConfirms(publisher)).repeatWhen(RepeatUtils.afterOneSecond(tryObtainLockMessage));
                }
        ).collect(Collectors.toList()));

        amqpEntities.getConsumerSpecifications().stream().filter(ConsumerSpecification::isAtMostOne).forEach(consumerSpecification -> {
            publishers.add(sendStopSignal(consumerSpecification.getQueue(), consumerSpecification.getLeaseTime()));
        });

        return Flux.fromIterable(amqpEntities.getExchangeSpecifications())
            .flatMap(sender::declareExchange)
            .thenMany(Flux.fromIterable(amqpEntities.getQueueSpecifications()))
            .flatMap(sender::declareQueue)
            .thenMany(Flux.fromIterable(amqpEntities.getBindingSpecifications()))
            .flatMap(sender::bind)

            .thenMany(Flux.fromIterable(publishers).flatMap(s -> s))
            .subscribe();
    }

    boolean command(Delivery delivery) {
        return CONSUMER_STOP_COMMAND.equals(new String(delivery.getBody()));
    }

    boolean notCommand(Delivery delivery) {
        return !command(delivery);
    }

    private Mono<Void> sendStopSignal(String queueName, Duration stopInterval) {
        Flux<OutboundMessage> stopMessages = Flux.interval(stopInterval)
                .delaySequence(stopInterval)
                .doOnNext(s -> LOGGER.info("Sending stop message"))
                .map(i -> stopMessage(queueName));

        Mono<Void> messageSender = flowOptions.getSender().send(stopMessages);

        String queueStop = queueName + ".stop";
        return flowLockMono.tryLock(queueStop, messageSender);
    }

    private OutboundMessage stopMessage(String queueName) {
//        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
//                .deliveryMode(1)
//                .build();
        return new OutboundMessage("", queueName, CONSUMER_STOP_COMMAND.getBytes());
    }
}
