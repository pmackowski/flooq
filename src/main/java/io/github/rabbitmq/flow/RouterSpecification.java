package io.github.rabbitmq.flow;

public class RouterSpecification {

    private String inputExchange;
    private String outputExchange;
    private String errorOutputExchange;
    private String intermediateQueue;
    private String routingKey;

    public static RouterSpecification router() {
        return new RouterSpecification();
    }

    public RouterSpecification inputExchange(String inputExchange) {
        this.inputExchange = inputExchange;
        return this;
    }

    public RouterSpecification outputExchange(String outputExchange) {
        this.outputExchange = outputExchange;
        return this;
    }

    public RouterSpecification errorOutputExchange(String errorOutputExchange) {
        this.errorOutputExchange = errorOutputExchange;
        return this;
    }

    public RouterSpecification intermediateQueue(String intermediateQueue) {
        this.intermediateQueue = intermediateQueue;
        return this;
    }

    public RouterSpecification routingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    public String getInputExchange() {
        return inputExchange;
    }

    public String getOutputExchange() {
        return outputExchange;
    }

    public String getErrorOutputExchange() {
        return errorOutputExchange;
    }

    public String getIntermediateQueue() {
        return intermediateQueue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    @Override
    public String toString() {
        return "inputExchange='" + inputExchange + '\'' +
                ", outputExchange='" + outputExchange + '\'' +
                ", errorOutputExchange='" + errorOutputExchange + '\'' +
                ", intermediateQueue='" + intermediateQueue + '\'' +
                ", routingKey='" + routingKey + '\'';

    }

}
