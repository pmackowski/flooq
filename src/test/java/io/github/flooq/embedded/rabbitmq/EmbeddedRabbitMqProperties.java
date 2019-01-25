package io.github.flooq.embedded.rabbitmq;

public class EmbeddedRabbitMqProperties {

    private String host = "localhost";
    private int port = 5672;
    private String username = "guest";
    private String password = "guest";
    private int timeoutInSeconds = 30;
    private String nodename = "rabbit@localhost";
    private boolean managemementPluginEnabled = false;

    public String getHost() {
        return host;
    }

    public EmbeddedRabbitMqProperties host(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public EmbeddedRabbitMqProperties port(int port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public EmbeddedRabbitMqProperties username(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public EmbeddedRabbitMqProperties password(String password) {
        this.password = password;
        return this;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public EmbeddedRabbitMqProperties timeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public String getNodename() {
        return nodename;
    }

    public EmbeddedRabbitMqProperties nodename(String nodename) {
        this.nodename = nodename;
        return this;
    }

    public boolean isManagemementPluginEnabled() {
        return managemementPluginEnabled;
    }

    public EmbeddedRabbitMqProperties managemementPluginEnabled(boolean managemementPluginEnabled) {
        this.managemementPluginEnabled = managemementPluginEnabled;
        return this;
    }

}
