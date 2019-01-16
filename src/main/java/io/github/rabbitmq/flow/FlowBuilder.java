package io.github.rabbitmq.flow;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class FlowBuilder {

    private final FlowOptions flowOptions;

    private Set<DefaultExchange> topics = new HashSet<>();
    private Set<DefaultConsumer> consumers = new HashSet<>();
    private Set<DefaultVirtualConsumer> virtualConsumers = new HashSet<>();
    private Set<DefaultShovel> shovels = new HashSet<>();
    private Set<DefaultShovelPartition> shovelPartitions = new HashSet<>();

    public FlowBuilder() {
        this(new FlowOptions());
    }

    public FlowBuilder(FlowOptions flowOptions) {
        this.flowOptions = flowOptions;
    }

    public FlowBuilder topic(String exchangeName) {
        DefaultExchange topic = (DefaultExchange) new DefaultExchange().exchange(exchangeName, ExchangeType.TOPIC);
        topics.add(topic);
        return this;
    }

    public FlowBuilder topic(Function<Exchange, Exchange> topicFunction) {
        DefaultExchange topic = (DefaultExchange) topicFunction.apply(new DefaultExchange().exchangeType(ExchangeType.TOPIC));
        topics.add(topic);
        return this;
    }

    public FlowBuilder topicPartition(String exchangeName) {
        DefaultExchange topic = (DefaultExchange) new DefaultExchange().exchange(exchangeName, ExchangeType.CONSISTENT_HASH);
        topics.add(topic);
        return this;
    }

    public FlowBuilder topicPartition(Function<Exchange, Exchange> topicPartitionFunction) {
        DefaultExchange topicPartition = (DefaultExchange) topicPartitionFunction.apply(new DefaultExchange().exchangeType(ExchangeType.CONSISTENT_HASH));
        topics.add(topicPartition);
        return this;
    }

    public FlowBuilder fromTopic(Function<Consumer, Consumer> consumerFunction) {
        DefaultConsumer consumer = (DefaultConsumer) consumerFunction.apply(new DefaultConsumer());
        consumers.add(consumer);
        return this;
    }

    public FlowBuilder fromTopicPartition(Function<VirtualConsumer, VirtualConsumer> virtualConsumerFunction) {
        DefaultVirtualConsumer virtualConsumer = (DefaultVirtualConsumer) virtualConsumerFunction.apply(new DefaultVirtualConsumer());
        virtualConsumers.add(virtualConsumer);
        return this;
    }

    public FlowBuilder shovel(Function<Shovel, Shovel> shovelFunction) {
        DefaultShovel shovel = (DefaultShovel) shovelFunction.apply(new DefaultShovel());
        shovels.add(shovel);
        return this;
    }

    public FlowBuilder shovelPartitions(Function<ShovelPartition, ShovelPartition> shovelFunction) {
        DefaultShovelPartition shovel = (DefaultShovelPartition) shovelFunction.apply(new DefaultShovelPartition());
        shovelPartitions.add(shovel);
        return this;
    }

    public Flow build() {
        AmqpEntities amqpEntities = new AmqpEntities("flow", topics, consumers, virtualConsumers, shovels, shovelPartitions);
        return new DefaultFlow(flowOptions, amqpEntities);
    }
}
