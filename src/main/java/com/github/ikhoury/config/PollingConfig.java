package com.github.ikhoury.config;

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
}
