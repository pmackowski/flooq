package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import reactor.rabbitmq.OutboundMessage;

import java.util.List;
import java.util.function.Function;

public interface ShovelPartition {

    ShovelPartition inputExchange(String inputExchange);

    ShovelPartition outputExchange(String outputExchange);

    ShovelPartition outputExchange(String outputExchange, ExchangeType exchangeType);

    ShovelPartition queue(String queue, int partitions);

    ShovelPartition buckets(List<Integer> buckets);

    ShovelPartition transform(Function<OutboundMessage, Publisher<OutboundMessage>> transform);

}
