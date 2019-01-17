package io.github.rabbitmq.flow;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

public interface ShovelPartition {

    ShovelPartition inputExchange(String inputExchange);

    ShovelPartition outputExchange(String outputExchange);

    ShovelPartition outputExchange(String outputExchange, ExchangeType exchangeType);

    ShovelPartition queue(String queue, int partitions);

    ShovelPartition buckets(List<Integer> buckets);

    ShovelPartition transform(Function<Flux<Delivery>,Flux<Delivery>> transform);

}
