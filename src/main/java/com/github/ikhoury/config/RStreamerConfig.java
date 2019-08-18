package com.github.ikhoury.config;

public class RStreamerConfig {

    private JedisConfig jedisConfig;
    private LeaseConfig leaseConfig;
    private PollingConfig pollingConfig;

    RStreamerConfig(JedisConfig jedisConfig, LeaseConfig leaseConfig, PollingConfig pollingConfig) {
        this.jedisConfig = jedisConfig;
        this.leaseConfig = leaseConfig;
        this.pollingConfig = pollingConfig;
    }

    public JedisConfig getJedisConfig() {
        return jedisConfig;
    }

    public LeaseConfig getLeaseConfig() {
        return leaseConfig;
    }

    public PollingConfig getPollingConfig() {
        return pollingConfig;
    }
}
