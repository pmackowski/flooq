package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import reactor.rabbitmq.OutboundMessage;

import java.util.function.Function;

public interface Shovel {

    Shovel inputExchange(String inputExchange);

    Shovel outputExchange(String outputExchange);

    Shovel outputExchange(String outputExchange, ExchangeType exchangeType);

    Shovel queue(String queue);

    Shovel routingKey(String routingKey);

    Shovel transform(Function<OutboundMessage, Publisher<OutboundMessage>> transform);
}
