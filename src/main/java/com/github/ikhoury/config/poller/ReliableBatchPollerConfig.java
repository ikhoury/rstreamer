package com.github.ikhoury.config.poller;

import java.util.Objects;

public class ReliableBatchPollerConfig {

    private int retryAttempts;
    private float connectionExceptionToleranceThreshold;

    public ReliableBatchPollerConfig(int retryAttempts, float connectionExceptionToleranceThreshold) {
        this.retryAttempts = retryAttempts;
        this.connectionExceptionToleranceThreshold = connectionExceptionToleranceThreshold;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public float getConnectionExceptionToleranceThreshold() {
        return connectionExceptionToleranceThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReliableBatchPollerConfig that = (ReliableBatchPollerConfig) o;
        return retryAttempts == that.retryAttempts &&
                Float.compare(that.connectionExceptionToleranceThreshold, connectionExceptionToleranceThreshold) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(retryAttempts, connectionExceptionToleranceThreshold);
    }

    @Override
    public String toString() {
        return "ReliableBatchPollerConfig{" +
                "retryAttempts=" + retryAttempts +
                ", connectionExceptionToleranceThreshold=" + connectionExceptionToleranceThreshold +
                '}';
    }
}
