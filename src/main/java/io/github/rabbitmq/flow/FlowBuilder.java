package io.github.rabbitmq.flow;

import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class FlowBuilder {

    private final FlowOptions flowOptions;

    private Set<Exchange> topics = new HashSet<>();
    private Set<Exchange> topicPartitions = new HashSet<>();
    private Set<Consumer> consumers = new HashSet<>();
    private Set<VirtualConsumer> virtualConsumers = new HashSet<>();
    private Set<Shovel> shovels = new HashSet<>();

    public FlowBuilder() {
        this(new FlowOptions());
    }

    public FlowBuilder(FlowOptions flowOptions) {
        this.flowOptions = flowOptions;
    }

    public FlowBuilder topic(String exchangeName) {
        Exchange topic = new ExchangeImpl().exchange(exchangeName, ExchangeType.TOPIC);
        topics.add(topic);
        return this;
    }

    public FlowBuilder topic(Function<Exchange, Exchange> topicFunction) {
        Exchange topic = topicFunction.apply(new ExchangeImpl().exchangeType(ExchangeType.TOPIC));
        topics.add(topic);
        return this;
    }

    public FlowBuilder topicPartition(String exchangeName) {
        Exchange topic = new ExchangeImpl().exchange(exchangeName, ExchangeType.CONSISTENT_HASH);
        topicPartitions.add(topic);
        return this;
    }

    public FlowBuilder topicPartition(Function<Exchange, Exchange> topicPartitionFunction) {
        Exchange topicPartition = topicPartitionFunction.apply(new ExchangeImpl().exchangeType(ExchangeType.CONSISTENT_HASH));
        topicPartitions.add(topicPartition);
        return this;
    }

    public FlowBuilder fromTopic(Function<Consumer, Consumer> consumerFunction) {
        Consumer consumer = consumerFunction.apply(new ConsumerImpl());
        consumers.add(consumer);
        return this;
    }

    public FlowBuilder fromTopicPartition(Function<VirtualConsumer, VirtualConsumer> virtualConsumerFunction) {
        VirtualConsumer virtualConsumer = virtualConsumerFunction.apply(new VirtualConsumerImpl());
        virtualConsumers.add(virtualConsumer);
        return this;
    }

    public FlowBuilder shovel(Function<Shovel, Shovel> shovelFunction) {
        Shovel shovel = shovelFunction.apply(new ShovelImpl());
        shovels.add(shovel);
        return this;
    }

    public Mono<Void> build() {
        // each implemented as a separate MonoOperator or FluxOperator, onSubscribe do the following
        // 1. declare resources (exchanges, bindings, queues)
        // 2. create consumers
        // 3. start all publishers
        return Mono.empty();
    }
}
