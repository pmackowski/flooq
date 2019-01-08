package io.github.rabbitmq.flow;

import com.rabbitmq.client.AMQP;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import java.util.LinkedHashMap;
import java.util.Map;

import static reactor.rabbitmq.BindingSpecification.binding;
import static reactor.rabbitmq.ExchangeSpecification.exchange;
import static reactor.rabbitmq.QueueSpecification.queue;

class RabbitMqDeclare {

    static final String DEAD_LETTER_EXCHANGE = "dlx.topic";
    static final String DEAD_LETTER_SUFFIX = ".dlq";

    private static final String EXCHANGE_TYPE = "topic";
    private static final int QUEUE_MAX_PRIORITY = 1;

    private final Sender sender;
    private final RabbitMqDeclareOptions options;

    RabbitMqDeclare(Sender sender) {
        this(sender, new RabbitMqDeclareOptions());
    }

    RabbitMqDeclare(Sender sender, RabbitMqDeclareOptions options) {
        this.sender = sender;
        this.options = options;
    }

    public Mono<?> declareExchange(String exchangeName) {
        return sender.declare(exchangeSpecification(exchangeName)).cache();
    }

    public Mono<AMQP.Queue.BindOk> declareAll(BindingSpecification bindingSpecification) {
        String exchangeName = bindingSpecification.getExchange();
        String queueName = bindingSpecification.getQueue() == null ? exchangeName : bindingSpecification.getQueue();
        String routingKey = bindingSpecification.getRoutingKey() == null ? "#" : bindingSpecification.getRoutingKey();
        return declareAllWithDeadLetterQueue(queueName, exchangeName, routingKey);
    }

    private Mono<AMQP.Queue.BindOk> declareAllWithDeadLetterQueue(String queueName, String exchangeName, String routingKey) {
        return sender.declareExchange(exchangeSpecification(exchangeName))
                .then(sender.declareQueue(queueSpecificationWithDeadLetterQueue(queueName)))
                .then(sender.bind(binding().exchange(exchangeName).queue(queueName).routingKey(routingKey)))
                .then(declareDeadLetterQueue(queueName))
                .cache();
    }

    private Mono<AMQP.Queue.BindOk> declareDeadLetterQueue(String queueName) {
        String deadLetterQueueName = queueName + DEAD_LETTER_SUFFIX;
        return declareExchange(DEAD_LETTER_EXCHANGE)
                .then(sender.declareQueue(queueSpecification(deadLetterQueueName)))
                .then(sender.bind(binding().exchange(DEAD_LETTER_EXCHANGE).queue(deadLetterQueueName).routingKey(queueName)));
    }

    private QueueSpecification queueSpecificationWithDeadLetterQueue(String queueName) {
        Map<String,Object> arguments = new LinkedHashMap<>();
        arguments.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", queueName);
        arguments.put("x-max-priority", QUEUE_MAX_PRIORITY);
        return queueSpecification(queueName)
                .arguments(arguments);
    }

    private QueueSpecification queueSpecification(String queueName) {
        return queue(queueName)
                .durable(options.isDurable());
    }

    private ExchangeSpecification exchangeSpecification(String exchangeName) {
        return exchange(exchangeName)
                .durable(options.isDurable())
                .type(EXCHANGE_TYPE);
    }


}
