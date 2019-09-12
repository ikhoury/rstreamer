package com.github.ikhoury.lease;

import com.github.ikhoury.config.subsription.LeaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * This class hands out leases to be run in a LeaseRunner. A limited number of leases
 * can be active at once. A lease should be returned so it can be reused.
 */
public class LeaseBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaseBroker.class);

    private final Semaphore leaseStore;
    private final int maxActiveLeases;
    private String queue;

    public LeaseBroker(LeaseConfig leaseConfig, String queue) {
        this.maxActiveLeases = leaseConfig.getMaxActiveLeases();
        this.leaseStore = new Semaphore(maxActiveLeases);
        this.queue = queue;
    }

    /**
     * This method guarantees that the caller will return with a lease.
     * If no lease is available then the caller will wait indefinitely for one.
     *
     * @return A lease
     */
    public Lease acquireLease() {
        leaseStore.acquireUninterruptibly();

        LOGGER.info("Acquired a lease for {}, {} estimated lease(s) left", queue, availableLeaseCount());
        return new Lease();
    }

    /**
     * Returns a lease so it can be reused
     *
     * @param lease The lease to return
     */
    void returnLease(Lease lease) {
        leaseStore.release();
        LOGGER.info("Returning a lease for {}", queue);
    }

    int activeLeaseCount() {
        return maxActiveLeases - leaseStore.availablePermits();
    }

    int availableLeaseCount() {
        return leaseStore.availablePermits();
    }
}
