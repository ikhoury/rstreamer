package com.github.ikhoury.config.poller;

public class BatchPollerConfigBuilder {

    private String host = "localhost";
    private int port = 6379;
    private int pollTimeoutInSeconds = 5;
    private int subscriptionCount = 8;

    private BatchPollerConfigBuilder() {

    }

    public static BatchPollerConfigBuilder defaultBatchPollerConfig() {
        return new BatchPollerConfigBuilder();
    }

    public BatchPollerConfigBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public BatchPollerConfigBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public BatchPollerConfigBuilder withPollTimeoutInSeconds(int seconds) {
        this.pollTimeoutInSeconds = seconds;
        return this;
    }

    public BatchPollerConfigBuilder withSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
        return this;
    }

    public BatchPollerConfig build() {
        return new BatchPollerConfig(host, port, pollTimeoutInSeconds, subscriptionCount);
    }
}
