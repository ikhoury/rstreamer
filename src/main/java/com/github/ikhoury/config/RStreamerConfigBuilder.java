package com.github.ikhoury.config;

import static com.github.ikhoury.config.JedisConfigBuilder.defaultJedisConfig;
import static com.github.ikhoury.config.LeaseConfigBuilder.defaultLeaseConfig;
import static com.github.ikhoury.config.PollingConfigBuilder.defaultPollingConfig;

public class RStreamerConfigBuilder {

    private JedisConfigBuilder jedisConfigBuilder = defaultJedisConfig();
    private LeaseConfigBuilder leaseConfigBuilder = defaultLeaseConfig();
    private PollingConfigBuilder pollingConfigBuilder = defaultPollingConfig();

    private RStreamerConfigBuilder() {

    }

    public static RStreamerConfigBuilder defaultRStreamerConfig() {
        return new RStreamerConfigBuilder();
    }

    public RStreamerConfigBuilder with(JedisConfigBuilder jedisConfigBuilder) {
        this.jedisConfigBuilder = jedisConfigBuilder;
        return this;
    }

    public RStreamerConfigBuilder with(LeaseConfigBuilder leaseConfigBuilder) {
        this.leaseConfigBuilder = leaseConfigBuilder;
        return this;
    }

    public RStreamerConfigBuilder with(PollingConfigBuilder pollingConfigBuilder) {
        this.pollingConfigBuilder = pollingConfigBuilder;
        return this;
    }

    public RStreamerConfig build() {
        return new RStreamerConfig(
                jedisConfigBuilder.build(),
                leaseConfigBuilder.build(),
                pollingConfigBuilder.build()
        );
    }
}
