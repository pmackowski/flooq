package io.github.rabbitmq.flow;

import org.reactivestreams.Publisher;
import reactor.rabbitmq.OutboundMessage;

public interface Exchange {

    /**
     * Exchange name that will be created by flow.
     * @param exchangeName
     * @return the current {@link Exchange} instance
     */
    Exchange exchange(String exchangeName);

    /**
     * Type of the exchange
     * @param exchangeType
     * @return the current {@link Exchange} instance
     */
    Exchange exchangeType(ExchangeType exchangeType);

    /**
     * Messages sent to the exchange {@link this#exchange(String)}.
     * Publishing is delayed until all flow resources are created.
     * @param publisher
     * @return the current {@link Exchange} instance
     */
    Exchange publisher(Publisher<OutboundMessage> publisher);

}
