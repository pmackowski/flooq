package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultShovelPartition implements ShovelPartition {

    private static final int DEFAULT_BUCKETS_PER_QUEUE = 1;

    private String inputExchange;
    private String outputExchange;
    private ExchangeType outputExchangeType;
    private String queue;
    private int partitions;
    private List<Integer> buckets = new ArrayList<>();
    private Function<Flux<Delivery>, Flux<Delivery>> transform;

    @Override
    public ShovelPartition inputExchange(String inputExchange) {
        this.inputExchange = inputExchange;
        return this;
    }

    @Override
    public ShovelPartition outputExchange(String outputExchange) {
        this.outputExchange = outputExchange;
        return this;
    }

    @Override
    public ShovelPartition outputExchange(String outputExchange, ExchangeType exchangeType) {
        this.outputExchange = outputExchange;
        this.outputExchangeType = exchangeType;
        return this;
    }

    @Override
    public ShovelPartition queue(String queue, int partitions) {
        this.queue = queue;
        this.partitions = partitions;
        if (buckets.isEmpty()) {
            buckets.addAll(IntStream.range(0,partitions).map(i -> DEFAULT_BUCKETS_PER_QUEUE).boxed().collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public ShovelPartition buckets(List<Integer> buckets) {
        this.buckets = buckets;
        return this;
    }

    @Override
    public ShovelPartition transform(Function<Flux<Delivery>, Flux<Delivery>> transform) {
        this.transform = transform;
        return this;
    }

    public String getInputExchange() {
        return inputExchange;
    }

    public String getOutputExchange() {
        return outputExchange;
    }

    public ExchangeType getOutputExchangeType() {
        return outputExchangeType;
    }

    public String getQueue() {
        return queue;
    }

    public int getPartitions() {
        return partitions;
    }

    public List<Integer> getBuckets() {
        return buckets;
    }

    public Function<Flux<Delivery>, Flux<Delivery>> getTransform() {
        return transform;
    }
}
