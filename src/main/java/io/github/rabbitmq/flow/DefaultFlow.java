package io.github.rabbitmq.flow;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.UUID;

class DefaultFlow implements Flow {

    private final FlowOptions flowOptions;
    private final AmqpEntities amqpEntities;

    DefaultFlow(FlowOptions flowOptions, AmqpEntities amqpEntities) {
        this.flowOptions = flowOptions;
        this.amqpEntities = amqpEntities;
    }

    @Override
    public void start() {
        Sender sender = flowOptions.getSender();

        Flux.fromIterable(amqpEntities.getExchangeSpecifications())
            .flatMap(sender::declareExchange)
            .thenMany(Flux.fromIterable(amqpEntities.getQueueSpecifications()))
            .flatMap(sender::declareQueue)
            .thenMany(Flux.fromIterable(amqpEntities.getBindingSpecifications()))
            .flatMap(sender::bind)
            .blockLast();

        Receiver receiver = flowOptions.getReceiver();

        amqpEntities.getConsumerSpecifications().forEach(
            consumerSpecification -> {
                if (consumerSpecification.getConsumeNoAck() != null) {
                    receiver.consumeNoAck(consumerSpecification.getQueue())
                            .transform(consumerSpecification.getConsumeNoAck())
                            .subscribe();
                } else if (consumerSpecification.getConsumeAutoAck() != null){
                    receiver.consumeAutoAck(consumerSpecification.getQueue())
                            .transform(consumerSpecification.getConsumeAutoAck())
                            .subscribeOn(Schedulers.elastic())
                            .subscribe();
                } else {
                    receiver.consumeManualAck(consumerSpecification.getQueue())
                            .transform(consumerSpecification.getConsumeManualAck())
                            .subscribe();
                }
            }
        );

        amqpEntities.getPublishers().forEach((exchange, publisher) -> {
            sender.send(publisher).subscribe();
        });

        Flux.range(0, 10000000)
                .map(i -> new OutboundMessage("exchangeName", UUID.randomUUID().toString(), "".getBytes()))
                .subscribeOn(Schedulers.elastic())
                .subscribe();

        try {
            Thread.sleep(1000 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
