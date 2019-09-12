package com.github.ikhoury.config.poller;

public class ReliableBatchPollerConfigBuilder {

    private int retryAttempts = 3;
    private float connectionExceptionToleranceThreshold = 50;

    private ReliableBatchPollerConfigBuilder() {

    }

    public static ReliableBatchPollerConfigBuilder defaultReliableBatchPollerConfig() {
        return new ReliableBatchPollerConfigBuilder();
    }

    public ReliableBatchPollerConfigBuilder withRetryAttempts(int retryAttempts) {
        if (retryAttempts < 1) {
            throw new IllegalArgumentException("retryAttempts must be greater than or equal to 1");
        }
        this.retryAttempts = retryAttempts;
        return this;
    }

    public ReliableBatchPollerConfigBuilder withConnectionExceptionToleranceThreshold(float connectionExceptionToleranceThreshold) {
        if (connectionExceptionToleranceThreshold <= 0 || connectionExceptionToleranceThreshold > 100) {
            throw new IllegalArgumentException("connectionExceptionToleranceThreshold must be between 1 and 100");
        }
        this.connectionExceptionToleranceThreshold = connectionExceptionToleranceThreshold;
        return this;
    }

    public ReliableBatchPollerConfig build() {
        return new ReliableBatchPollerConfig(retryAttempts, connectionExceptionToleranceThreshold);
    }
}
