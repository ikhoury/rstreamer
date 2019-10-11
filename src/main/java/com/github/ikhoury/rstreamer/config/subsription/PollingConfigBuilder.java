package com.github.ikhoury.rstreamer.config.subsription;

public class PollingConfigBuilder {

    private int batchSize = 100;
    private int batchSizeThreshold = 20;

    private PollingConfigBuilder() {

    }

    public static PollingConfigBuilder defaultPollingConfig() {
        return new PollingConfigBuilder();
    }

    public PollingConfigBuilder withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public PollingConfigBuilder withBatchSizeThreshold(int batchSizeThreshold) {
        this.batchSizeThreshold = batchSizeThreshold;
        return this;
    }

    public PollingConfig build() {
        return new PollingConfig(batchSize, batchSizeThreshold);
    }
}
