package io.github.flooq.embedded.rabbitmq;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class EmbeddedRabbitMqServerTest {

    @Test
    public void test() throws IOException {
        EmbeddedRabbitMqProperties embeddedRabbitMqProperties = new EmbeddedRabbitMqProperties()
                    .managemementPluginEnabled(true)
                    .port(5777)
                    .nodename("rabbit-dev");

        EmbeddedRabbitMqServer embeddedRabbitMqServer = new EmbeddedRabbitMqServer(embeddedRabbitMqProperties);
        embeddedRabbitMqServer.start();
        embeddedRabbitMqServer.stop();
    }

}
