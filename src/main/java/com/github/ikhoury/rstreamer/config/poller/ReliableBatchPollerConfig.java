package com.github.ikhoury.rstreamer.config.poller;

import java.util.Objects;

public class ReliableBatchPollerConfig {

    private final int retryAttempts;
    private final int sampleCountMultiplier;
    private final float failureRateThreshold;

    ReliableBatchPollerConfig(int retryAttempts, float failureRateThreshold, int sampleCountMultiplier) {
        this.retryAttempts = retryAttempts;
        this.failureRateThreshold = failureRateThreshold;
        this.sampleCountMultiplier = sampleCountMultiplier;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public int getSampleCountMultiplier() {
        return sampleCountMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReliableBatchPollerConfig that = (ReliableBatchPollerConfig) o;
        return retryAttempts == that.retryAttempts &&
                sampleCountMultiplier == that.sampleCountMultiplier &&
                Float.compare(that.failureRateThreshold, failureRateThreshold) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(retryAttempts, sampleCountMultiplier, failureRateThreshold);
    }

    @Override
    public String toString() {
        return "ReliableBatchPollerConfig{" +
                "retryAttempts=" + retryAttempts +
                ", sampleCountMultiplier=" + sampleCountMultiplier +
                ", failureRateThreshold=" + failureRateThreshold +
                '}';
    }
}
