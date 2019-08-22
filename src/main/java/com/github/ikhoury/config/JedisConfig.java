package com.github.ikhoury.config;

import java.util.Objects;

public class JedisConfig {

    private String host;
    private int port;
    private int pollTimeoutInSeconds;
    private int subscriptionCount;

    JedisConfig(String host, int port, int pollTimeoutInSeconds, int subscriptionCount) {
        this.host = host;
        this.port = port;
        this.pollTimeoutInSeconds = pollTimeoutInSeconds;
        this.subscriptionCount = subscriptionCount;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getPollTimeoutInSeconds() {
        return pollTimeoutInSeconds;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JedisConfig that = (JedisConfig) o;
        return port == that.port &&
                pollTimeoutInSeconds == that.pollTimeoutInSeconds &&
                subscriptionCount == that.subscriptionCount &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, pollTimeoutInSeconds, subscriptionCount);
    }

    @Override
    public String toString() {
        return "JedisConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", pollTimeoutInSeconds=" + pollTimeoutInSeconds +
                ", subscriptionCount=" + subscriptionCount +
                '}';
    }
}
