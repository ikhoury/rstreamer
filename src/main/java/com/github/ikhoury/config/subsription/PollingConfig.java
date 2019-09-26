package com.github.ikhoury.config.subsription;

import java.util.Objects;

public class PollingConfig {

    private int batchSize;
    private int batchSizeThreshold;

    PollingConfig(int batchSize, int batchSizeThreshold) {
        this.batchSize = batchSize;
        this.batchSizeThreshold = batchSizeThreshold;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getBatchSizeThreshold() {
        return batchSizeThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PollingConfig that = (PollingConfig) o;
        return batchSize == that.batchSize &&
                batchSizeThreshold == that.batchSizeThreshold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchSize, batchSizeThreshold);
    }

    @Override
    public String toString() {
        return "PollingConfig{" +
                "batchSize=" + batchSize +
                ", batchSizeThreshold=" + batchSizeThreshold +
                '}';
    }
}
