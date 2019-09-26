package com.github.ikhoury.config.poller;

import java.util.Objects;

public class BatchPollerConfig {

    private String host;
    private int port;
    private int pollTimeoutInSeconds;

    BatchPollerConfig(String host, int port, int pollTimeoutInSeconds) {
        this.host = host;
        this.port = port;
        this.pollTimeoutInSeconds = pollTimeoutInSeconds;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchPollerConfig that = (BatchPollerConfig) o;
        return port == that.port &&
                pollTimeoutInSeconds == that.pollTimeoutInSeconds &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, pollTimeoutInSeconds);
    }

    @Override
    public String toString() {
        return "BatchPollerConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", pollTimeoutInSeconds=" + pollTimeoutInSeconds +
                '}';
    }
}
