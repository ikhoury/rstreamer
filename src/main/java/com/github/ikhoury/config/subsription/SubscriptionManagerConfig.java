package com.github.ikhoury.config.subsription;

import java.util.Objects;

public class SubscriptionManagerConfig {

    private final LeaseConfig leaseConfig;
    private final PollingConfig pollingConfig;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionManagerConfig config = (SubscriptionManagerConfig) o;
        return Objects.equals(leaseConfig, config.leaseConfig) &&
                Objects.equals(pollingConfig, config.pollingConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaseConfig, pollingConfig);
    }

    @Override
    public String toString() {
        return "SubscriptionManagerConfig{" +
                "leaseConfig=" + leaseConfig +
                ", pollingConfig=" + pollingConfig +
                '}';
    }
}
