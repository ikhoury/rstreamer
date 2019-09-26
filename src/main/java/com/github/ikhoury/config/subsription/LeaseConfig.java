package com.github.ikhoury.config.subsription;

import java.util.Objects;

public class LeaseConfig {

    private int maxActiveLeases;

    public LeaseConfig(int maxActiveLeases) {
        this.maxActiveLeases = maxActiveLeases;
    }

    public int getMaxActiveLeases() {
        return maxActiveLeases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaseConfig that = (LeaseConfig) o;
        return maxActiveLeases == that.maxActiveLeases;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxActiveLeases);
    }

    @Override
    public String toString() {
        return "LeaseConfig{" +
                "maxActiveLeases=" + maxActiveLeases +
                '}';
    }
}
