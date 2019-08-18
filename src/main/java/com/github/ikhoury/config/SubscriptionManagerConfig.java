package com.github.ikhoury.config;

public class SubscriptionManagerConfig {

    private LeaseConfig leaseConfig;
    private PollingConfig pollingConfig;

    SubscriptionManagerConfig(LeaseConfig leaseConfig, PollingConfig pollingConfig) {
        this.leaseConfig = leaseConfig;
        this.pollingConfig = pollingConfig;
    }

    public LeaseConfig getLeaseConfig() {
        return leaseConfig;
    }

    public PollingConfig getPollingConfig() {
        return pollingConfig;
    }
}
