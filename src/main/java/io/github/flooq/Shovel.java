package io.github.flooq;

import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public interface Shovel {

    Shovel inputExchange(String inputExchange);

    Shovel outputExchange(String outputExchange);

    Shovel outputExchange(String outputExchange, ExchangeType exchangeType);

    Shovel queue(String queue);

    Shovel routingKey(String routingKey);

    Shovel transform(Function<Flux<Delivery>,Flux<Delivery>> transform);
}
