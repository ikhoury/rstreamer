package com.github.ikhoury.config;

import java.util.Objects;

import static com.github.ikhoury.constants.RedisConfigConstants.DEFAULT_HOST;
import static com.github.ikhoury.constants.RedisConfigConstants.DEFAULT_PORT;

public class RedisConfig {
    private String host;
    private int port;

    public RedisConfig() {
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
    }

    public RedisConfig(String host, int port) {
        this.host = host;
        this.port = port;
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

    @Override
    public String toString() {
        return "RedisConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisConfig that = (RedisConfig) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
