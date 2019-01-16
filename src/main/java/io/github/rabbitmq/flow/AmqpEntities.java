package io.github.rabbitmq.flow;

import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.QueueSpecification;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class AmqpEntities {

    private String entitiesPrefix;
    private Set<DefaultExchange> topics;
    private Set<DefaultConsumer> consumers;
    private Set<DefaultVirtualConsumer> virtualConsumers;
    private Set<DefaultShovel> shovels;
    private Set<DefaultShovelPartition> shovelPartitions;

    public AmqpEntities(String entitiesPrefix,
                        Set<DefaultExchange> topics,
                        Set<DefaultConsumer> consumers,
                        Set<DefaultVirtualConsumer> virtualConsumers,
                        Set<DefaultShovel> shovels,
                        Set<DefaultShovelPartition> shovelPartitions) {
        this.entitiesPrefix = entitiesPrefix;
        this.topics = topics;
        this.consumers = consumers;
        this.virtualConsumers = virtualConsumers;
        this.shovels = shovels;
        this.shovelPartitions = shovelPartitions;
    }

    Set<ExchangeSpecification> getExchangeSpecifications() {
        Set<ExchangeName> exchangeNames = new HashSet<>();
        exchangeNames.addAll(topics.stream().map(exchange -> new ExchangeName(exchange.getExchange(), exchange.getExchangeType())).collect(Collectors.toSet()));
        exchangeNames.addAll(consumers.stream().map(consumer -> new ExchangeName(consumer.getInputExchange(), ExchangeType.TOPIC)).collect(Collectors.toSet()));
        exchangeNames.addAll(virtualConsumers.stream().map(virtualConsumer -> new ExchangeName(virtualConsumer.getInputExchange(), ExchangeType.CONSISTENT_HASH)).collect(Collectors.toSet()));
        exchangeNames.addAll(shovels.stream().map(shovel -> new ExchangeName(shovel.getInputExchange(), ExchangeType.TOPIC)).collect(Collectors.toSet()));
        // TODO it does not have to be topic
        exchangeNames.addAll(shovels.stream().map(shovel -> new ExchangeName(shovel.getOutputExchange(), ExchangeType.TOPIC)).collect(Collectors.toSet()));
        exchangeNames.addAll(shovelPartitions.stream().map(shovelPartitions -> new ExchangeName(shovelPartitions.getInputExchange(), ExchangeType.CONSISTENT_HASH)).collect(Collectors.toSet()));
        exchangeNames.addAll(shovelPartitions.stream().map(shovelPartitions -> new ExchangeName(shovelPartitions.getOutputExchange(), shovelPartitions.getOutputExchangeType())).collect(Collectors.toSet()));

        return exchangeNames.stream().map(exchangeName -> new ExchangeSpecification()
                .name(exchangeName.getName())
                .type(exchangeName.getType())
        ).collect(Collectors.toSet());
    }

    Set<QueueSpecification> getQueueSpecifications() {
        Set<String> queueNames = new HashSet<>();
        queueNames.addAll(consumers.stream().map(DefaultConsumer::getQueue).collect(Collectors.toSet()));
        queueNames.addAll(shovels.stream().map(DefaultShovel::getQueue).collect(Collectors.toSet()));
        queueNames.addAll(virtualConsumers.stream()
                .flatMap(defaultVirtualConsumer -> IntStream.range(0, defaultVirtualConsumer.getPartitions()).mapToObj(i -> defaultVirtualConsumer.getQueueName() + "." + i))
                .collect(Collectors.toSet()));
        queueNames.addAll(shovelPartitions.stream()
                .flatMap(defaultShovelPartition -> IntStream.range(0, defaultShovelPartition.getPartitions()).mapToObj(i -> defaultShovelPartition.getQueue() + "." + i))
                .collect(Collectors.toSet()));

        return queueNames.stream().map(queueName -> new QueueSpecification().name(queueName)).collect(Collectors.toSet());
    }

    Set<BindingSpecification> getBindingSpecifications() {
        Set<BindingSpecification> result = new HashSet<>();
        result.addAll(consumers.stream().map(defaultConsumer -> new BindingSpecification()
                .routingKey(defaultConsumer.getRoutingKey())
                .queue(defaultConsumer.getQueue())
                .exchange(defaultConsumer.getInputExchange())).collect(Collectors.toSet()));
        result.addAll(virtualConsumers.stream().flatMap(defaultVirtualConsumer -> IntStream.range(0, defaultVirtualConsumer.getPartitions())
                .mapToObj(i -> new BindingSpecification()
                        .exchange(defaultVirtualConsumer.getInputExchange())
                        .queue(defaultVirtualConsumer.getQueueName() + "." + i)
                        .routingKey(String.valueOf(defaultVirtualConsumer.getBuckets().get(i)))
                )).collect(Collectors.toSet()));
        result.addAll(shovels.stream().map(defaultShovel -> new BindingSpecification()
                .routingKey(defaultShovel.getRoutingKey())
                .queue(defaultShovel.getQueue())
                .exchange(defaultShovel.getInputExchange())).collect(Collectors.toSet()));
        result.addAll(shovelPartitions.stream().flatMap(defaultShovelPartition -> IntStream.range(0, defaultShovelPartition.getPartitions())
                .mapToObj(i -> new BindingSpecification()
                        .exchange(defaultShovelPartition.getInputExchange())
                        .queue(defaultShovelPartition.getQueue() + "." + i)
                        .routingKey(String.valueOf(defaultShovelPartition.getBuckets().get(i)))
                )).collect(Collectors.toSet()));
        return result;
    }

    private static class ExchangeName {
        private String name;
        private String type;

        public ExchangeName(String name, ExchangeType exchangeType) {
            this.name = name;
            this.type = exchangeType.getValue();
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExchangeName that = (ExchangeName) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }

    }

}
