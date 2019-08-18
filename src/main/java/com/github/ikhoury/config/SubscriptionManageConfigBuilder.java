package com.github.ikhoury.config;

import static com.github.ikhoury.config.LeaseConfigBuilder.defaultLeaseConfig;
import static com.github.ikhoury.config.PollingConfigBuilder.defaultPollingConfig;

public class SubscriptionManageConfigBuilder {

    private LeaseConfigBuilder leaseConfigBuilder = defaultLeaseConfig();
    private PollingConfigBuilder pollingConfigBuilder = defaultPollingConfig();

    private SubscriptionManageConfigBuilder() {

    }

    public static SubscriptionManageConfigBuilder defaultSubscriptionManagerConfig() {
        return new SubscriptionManageConfigBuilder();
    }

    public SubscriptionManageConfigBuilder with(LeaseConfigBuilder leaseConfigBuilder) {
        this.leaseConfigBuilder = leaseConfigBuilder;
        return this;
    }

    public SubscriptionManageConfigBuilder with(PollingConfigBuilder pollingConfigBuilder) {
        this.pollingConfigBuilder = pollingConfigBuilder;
        return this;
    }

    public SubscriptionManagerConfig build() {
        return new SubscriptionManagerConfig(
                leaseConfigBuilder.build(),
                pollingConfigBuilder.build()
        );
    }
}
