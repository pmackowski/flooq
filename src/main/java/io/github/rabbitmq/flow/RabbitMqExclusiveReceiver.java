package io.github.rabbitmq.flow;

import com.rabbitmq.client.AMQP;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.time.Duration;
import java.util.function.Function;

public class RabbitMqExclusiveReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqExclusiveReceiver.class);

    private final RabbitMqReceiver receiver;
    private final Sender sender;
    private final MonoLock monoLock;

    public RabbitMqExclusiveReceiver(RabbitMqReceiver receiver, Sender sender, MonoLock monoLock) {
        this.receiver = receiver;
        this.sender = sender;
        this.monoLock = monoLock;
    }

    public <T extends FlowEvent, W> void consumeManualAck(BindingSpecification bindingSpecification, Class<T> type, Function<Flux<FlowEventAckDelivery<T>>, ? extends Publisher<W>> f) {
        consumeManualAck(bindingSpecification, type, new ConsumeExclusiveProperties(), f);
    }

    public <T extends FlowEvent, W> void consumeManualAck(BindingSpecification bindingSpecification, Class<T> type, ConsumeExclusiveProperties consumeExclusiveProperties, Function<Flux<FlowEventAckDelivery<T>>, ? extends Publisher<W>> f) {
        ConsumeProperties consumeProperties = new ConsumeProperties()
                .retry(false)
                .repeatable(false)
                .qos(consumeExclusiveProperties.getQos());

        Flux<W> flux = receiver.consumeManualAck(bindingSpecification, type, consumeProperties)
                .transform(f)
                .onErrorResume(e -> {
                    if ("Not retryable exception, cannot retry".equals(e.getMessage())) {
                        LOGGER.info("Consumer has been (probably) closed due to stop signal. Lock for queue {} is going to be released.", bindingSpecification.getQueue());
                    }
                    return Flux.empty();
                });

        monoLock
                .tryLockForQueue(bindingSpecification.getQueue(), flux)
                // TODO repeat
                // TODO retry
                .subscribe();

        sendStopSignal(bindingSpecification.getQueue(), consumeExclusiveProperties.getStopInterval());
    }

    private void sendStopSignal(String queueName, Duration stopInterval) {
        Flux<OutboundMessage> stopMessages = Flux.interval(stopInterval)
                .delaySequence(stopInterval)
                .map(i -> stopMessage(queueName));

        Mono<Void> messageSender = sender.send(stopMessages); // TODO retry ?

        String queueStop = queueName + ".stop";
        monoLock.tryLock(queueStop, messageSender).subscribe();
    }

    private OutboundMessage stopMessage(String queueName) {
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .deliveryMode(1) // non_persistent
                .priority(1) // default is 0
                .build();
        return new OutboundMessage("", queueName, basicProperties, ConsumeProperties.CONSUMER_STOP_COMMAND.getBytes());
    }

}
