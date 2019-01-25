package io.github.flooq.embedded.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EmbeddedRabbitMqServer {

    private Logger log = LoggerFactory.getLogger(EmbeddedRabbitMqServer.class);

    private static final String START_COMMAND = "rabbitmq-server";
    private static final String STOP_COMMAND = "rabbitmqctl";
    private static final String WINDOWS_EXT_COMMAND = ".bat";

    private final EmbeddedRabbitMqProperties embeddedRabbitProperties;

    public EmbeddedRabbitMqServer() {
        this.embeddedRabbitProperties = new EmbeddedRabbitMqProperties();
    }

    public EmbeddedRabbitMqServer(EmbeddedRabbitMqProperties embeddedRabbitProperties) {
        this.embeddedRabbitProperties = embeddedRabbitProperties;
    }

    public void start() throws IOException {
        log.info("Starting RabbitMq (port: {})...", embeddedRabbitProperties.getPort());
        CountDownLatch countDownLatch = new CountDownLatch(1);

        new ProcessExecutor()
                .command(command(START_COMMAND))
                .environment(environment())
                .destroyOnExit()
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        log.info(line);
                        if (line.contains("completed with")) {
                            countDownLatch.countDown();
                        }
                    }
                })
                .start();
        try {
            countDownLatch.await(embeddedRabbitProperties.getTimeoutInSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new EmbeddedRabbitMqException(String.format("Cannot obtain connection to RabbitMq (port: %s)!", embeddedRabbitProperties.getPort()), e);
        }
    }

    public void stop()  {
        log.info("Stopping RabbitMq (port: {})...", embeddedRabbitProperties.getPort());
        try {
            new ProcessExecutor()
                    .command(command(STOP_COMMAND), "-n", embeddedRabbitProperties.getNodename(), "stop")
                    .redirectOutput(System.out)
                    .execute();
        } catch (Exception e) {
            throw new EmbeddedRabbitMqException(String.format("Cannot stop RabbitMq (port: %s)!", embeddedRabbitProperties.getPort()), e);
        }
    }

    private Map<String, String> environment() {
        Map<String,String> environmant = new HashMap<>();
        environmant.put("RABBITMQ_NODE_PORT", String.valueOf(embeddedRabbitProperties.getPort()));
        environmant.put("RABBITMQ_NODENAME", embeddedRabbitProperties.getNodename());
        if (embeddedRabbitProperties.isManagemementPluginEnabled()) {
            environmant.put("RABBITMQ_SERVER_START_ARGS", String.format("-rabbitmq_management listener [{port,1%s}]", embeddedRabbitProperties.getPort()));
        }
        return environmant;
    }

    private String command(String command) {
        boolean windowsOperatingSystem = System.getProperty("os.name").toLowerCase().contains("win");
        return windowsOperatingSystem ? command + WINDOWS_EXT_COMMAND : command;
    }

}
