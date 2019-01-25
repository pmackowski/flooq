package io.github.flooq.docs;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.flooq.FlooqBuilder;
import io.github.flooq.FlooqOptions;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

public class FlooqConfigurationApi {

    // tag::spring[]
    @Bean
    public Mono<? extends Connection> monoConnection() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        // ...                                                      // <1>
        return Utils.singleConnectionMono(connectionFactory);       // <2>
    }

    @Bean
    public Receiver receiver(Mono<? extends Connection> monoConnection) {
        return RabbitFlux.createReceiver(
                new ReceiverOptions().connectionMono(monoConnection)
        );
    }

    @Bean
    public Sender sender(Mono<? extends Connection> monoConnection) {
        return RabbitFlux.createSender(
                new SenderOptions().connectionMono(monoConnection)
        );
    }

    @Bean
    public FlooqBuilder flooqBuilder(Receiver receiver, Sender sender) {
        FlooqOptions flooqOptions = new FlooqOptions()
                .receiver(receiver)
                .sender(sender)
                //.flooqLock(flooqLock)
                ;

        return new FlooqBuilder(flooqOptions);
    }
    // end::spring[]

    @interface Bean {}

}
