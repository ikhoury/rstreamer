package com.github.ikhoury.lease;

import com.github.ikhoury.consumer.PollingRoutine;
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

    public LeaseBroker(int maxActiveLeases) {
        this.leaseStore = new Semaphore(maxActiveLeases);
    }

    /**
     * This method guarantees that the caller will return with a lease.
     * If no lease is available then the caller will wait indefinitely for one.
     *
     * @param routine The routine that will be run by the LeaseRunner
     * @return A Lease for the task
     */
    public Lease acquireLeaseFor(PollingRoutine routine) {
        leaseStore.acquireUninterruptibly();

        String name = routine.getWorkQueue();
        LOGGER.info("Acquired a lease for {}, {} estimated lease(s) left", routine.getWorkQueue(), leaseStore.availablePermits());
        return new Lease(name, routine::doPoll);
    }

    public void returnLease(Lease lease) {
        leaseStore.release();
        LOGGER.info("Released a lease for {}", lease.getName());
    }
}
