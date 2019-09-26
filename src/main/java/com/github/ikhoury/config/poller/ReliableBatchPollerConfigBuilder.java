package com.github.ikhoury.config.poller;

public class ReliableBatchPollerConfigBuilder {

    private int retryAttempts = 3;
    private int sampleCountMultiplier = 5;
    private float failureRateThreshold = 50;

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

    public ReliableBatchPollerConfigBuilder withFailureRateThreshold(float failureRateThreshold) {
        if (failureRateThreshold <= 0 || failureRateThreshold > 100) {
            throw new IllegalArgumentException("failureRateThreshold must be between 1 and 100");
        }

        this.failureRateThreshold = failureRateThreshold;
        return this;
    }

    public ReliableBatchPollerConfigBuilder withSampleCountMultiplier(int sampleCountMultiplier) {
        if (sampleCountMultiplier <= 0 || sampleCountMultiplier > 10) {
            throw new IllegalArgumentException("sampleCountMultiplier must be between 1 and 10");
        }

        this.sampleCountMultiplier = sampleCountMultiplier;
        return this;
    }

    public ReliableBatchPollerConfig build() {
        return new ReliableBatchPollerConfig(retryAttempts, failureRateThreshold, sampleCountMultiplier);
    }
}
