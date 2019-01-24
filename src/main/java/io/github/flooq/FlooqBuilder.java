package io.github.flooq;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class FlooqBuilder {

    private final FlooqOptions flooqOptions;

    private Set<DefaultExchange> topics = new HashSet<>();
    private Set<DefaultConsumer> consumers = new HashSet<>();
    private Set<DefaultVirtualConsumer> virtualConsumers = new HashSet<>();
    private Set<DefaultShovel> shovels = new HashSet<>();
    private Set<DefaultShovelPartition> shovelPartitions = new HashSet<>();

    public FlooqBuilder() {
        this(new FlooqOptions());
    }

    public FlooqBuilder(FlooqOptions flooqOptions) {
        this.flooqOptions = flooqOptions;
    }

    public FlooqBuilder topic(String exchangeName) {
        DefaultExchange topic = (DefaultExchange) new DefaultExchange().exchange(exchangeName, ExchangeType.TOPIC);
        topics.add(topic);
        return this;
    }

    public FlooqBuilder topic(Function<Exchange, Exchange> topicFunction) {
        DefaultExchange topic = (DefaultExchange) topicFunction.apply(new DefaultExchange().exchangeType(ExchangeType.TOPIC));
        topics.add(topic);
        return this;
    }

    public FlooqBuilder topicPartition(String exchangeName) {
        DefaultExchange topic = (DefaultExchange) new DefaultExchange().exchange(exchangeName, ExchangeType.CONSISTENT_HASH);
        topics.add(topic);
        return this;
    }

    public FlooqBuilder topicPartition(Function<Exchange, Exchange> topicPartitionFunction) {
        DefaultExchange topicPartition = (DefaultExchange) topicPartitionFunction.apply(new DefaultExchange().exchangeType(ExchangeType.CONSISTENT_HASH));
        topics.add(topicPartition);
        return this;
    }

    public FlooqBuilder fromTopic(Function<Consumer, Consumer> consumerFunction) {
        DefaultConsumer consumer = (DefaultConsumer) consumerFunction.apply(new DefaultConsumer());
        consumers.add(consumer);
        return this;
    }

    public FlooqBuilder fromTopicPartition(Function<VirtualConsumer, VirtualConsumer> virtualConsumerFunction) {
        DefaultVirtualConsumer virtualConsumer = (DefaultVirtualConsumer) virtualConsumerFunction.apply(new DefaultVirtualConsumer());
        virtualConsumers.add(virtualConsumer);
        return this;
    }

    public FlooqBuilder shovel(Function<Shovel, Shovel> shovelFunction) {
        DefaultShovel shovel = (DefaultShovel) shovelFunction.apply(new DefaultShovel());
        shovels.add(shovel);
        return this;
    }

    public FlooqBuilder shovelPartitions(Function<ShovelPartition, ShovelPartition> shovelFunction) {
        DefaultShovelPartition shovel = (DefaultShovelPartition) shovelFunction.apply(new DefaultShovelPartition());
        shovelPartitions.add(shovel);
        return this;
    }

    public Flooq build() {
        AmqpEntities amqpEntities = new AmqpEntities("flooq", topics, consumers, virtualConsumers, shovels, shovelPartitions);
        return new DefaultFlooq(flooqOptions, amqpEntities);
    }
}
