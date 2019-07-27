package com.github.ikhoury.config;

import java.util.Objects;

public class JedisConfig {

    private String host;
    private int port;
    private int pollTimeoutInSeconds;

    JedisConfig(String host, int port, int pollTimeoutInSeconds) {
        this.host = host;
        this.port = port;
        this.pollTimeoutInSeconds = pollTimeoutInSeconds;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPollTimeoutInSeconds() {
        return pollTimeoutInSeconds;
    }

    public void setPollTimeoutInSeconds(int pollTimeoutInSeconds) {
        this.pollTimeoutInSeconds = pollTimeoutInSeconds;
    }

    @Override
    public String toString() {
        return "JedisConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", pollTimeoutInSeconds=" + pollTimeoutInSeconds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JedisConfig that = (JedisConfig) o;
        return port == that.port &&
                pollTimeoutInSeconds == that.pollTimeoutInSeconds &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, pollTimeoutInSeconds);
    }
}
