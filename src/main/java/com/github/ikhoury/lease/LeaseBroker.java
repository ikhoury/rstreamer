package com.github.ikhoury.lease;

import com.github.ikhoury.config.LeaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * This class hands out leases to be run in a LeaseRunner. Only a specified number of leases
 * can be handed out. A lease owner should return the lease after running for it to be reused.
 */
public class LeaseBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaseBroker.class);

    private final Semaphore leaseStore;
    private final int maxActiveLeases;

    public LeaseBroker(LeaseConfig leaseConfig) {
        this.maxActiveLeases = leaseConfig.getMaxActiveLeases();
        this.leaseStore = new Semaphore(maxActiveLeases);
    }

    /**
     * This method guarantees that the caller will return with a lease.
     * If no lease is available then the caller will wait indefinitely for one.
     *
     * @param name Description for the lease
     * @return A Lease for the task
     */
    public Lease acquireLeaseFor(String name) {
        leaseStore.acquireUninterruptibly();

        LOGGER.info("Acquired a lease for {}, {} estimated lease(s) left", name, availableLeaseCount());
        return new Lease(name);
    }

    void returnLease(Lease lease) {
        leaseStore.release();
        LOGGER.info("Released a lease for {}", lease.getName());
    }

    int activeLeaseCount() {
        return maxActiveLeases - leaseStore.availablePermits();
    }

    int availableLeaseCount() {
        return leaseStore.availablePermits();
    }
}
