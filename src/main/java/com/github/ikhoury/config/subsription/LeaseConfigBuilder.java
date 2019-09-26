package com.github.ikhoury.config.subsription;

public class LeaseConfigBuilder {

    private int availableLeases = 1;

    private LeaseConfigBuilder() {

    }

    public static LeaseConfigBuilder defaultLeaseConfig() {
        return new LeaseConfigBuilder();
    }

    public LeaseConfigBuilder withAvailableLeases(int availableLeases) {
        this.availableLeases = availableLeases;
        return this;
    }

    public LeaseConfig build() {
        return new LeaseConfig(availableLeases);
    }
}
