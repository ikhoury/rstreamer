package com.github.ikhoury.config;

public class LeaseConfig {

    private int maxActiveLeases;

    public LeaseConfig(int maxActiveLeases) {
        this.maxActiveLeases = maxActiveLeases;
    }

    public int getMaxActiveLeases() {
        return maxActiveLeases;
    }
}
