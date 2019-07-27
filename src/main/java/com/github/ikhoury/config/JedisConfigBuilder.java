package com.github.ikhoury.config;

public class JedisConfigBuilder {

    private String host = "localhost";
    private int port = 6379;
    private int pollTimeoutInSeconds = 5;

    private JedisConfigBuilder() {

    }

    public static JedisConfigBuilder defaultJedisConfig() {
        return new JedisConfigBuilder();
    }

    public JedisConfigBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public JedisConfigBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public JedisConfigBuilder withPollTimeoutInSeconds(int seconds) {
        this.pollTimeoutInSeconds = seconds;
        return this;
    }

    public JedisConfig build() {
        return new JedisConfig(host, port, pollTimeoutInSeconds);
    }
}
