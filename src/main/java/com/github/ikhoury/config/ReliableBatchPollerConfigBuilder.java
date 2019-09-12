package com.github.ikhoury.config;

public class ReliableBatchPollerConfigBuilder {

    private int retryAttempts = 3;
    private float connectionExceptionToleranceThreshold = 0.5F;

    private ReliableBatchPollerConfigBuilder() {

    }

    public static ReliableBatchPollerConfigBuilder defaultReliableBatchPollerConfig() {
        return new ReliableBatchPollerConfigBuilder();
    }

    public ReliableBatchPollerConfigBuilder withRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
        return this;
    }

    public ReliableBatchPollerConfigBuilder withConnectionExceptionToleranceThreshold(float connectionExceptionToleranceThreshold) {
        this.connectionExceptionToleranceThreshold = connectionExceptionToleranceThreshold;
        return this;
    }

    public ReliableBatchPollerConfig build() {
        return new ReliableBatchPollerConfig(retryAttempts, connectionExceptionToleranceThreshold);
    }
}
