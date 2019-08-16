package com.github.ikhoury.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Runs leases asynchronously.
 */
public class LeaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaseRunner.class);

    private final Executor executor;
    private final LeaseBroker leaseBroker;

    public LeaseRunner(LeaseBroker leaseBroker, Executor executor) {
        this.executor = executor;
        this.leaseBroker = leaseBroker;
    }

    public void run(Lease lease) {
        LOGGER.info("Running lease for {}", lease.getName());
        executor.execute(() -> {
            try {
                lease.getTask().run();
            } catch (Throwable throwable) {
                LOGGER.error("Failed to run lease for {}", lease.getName(), throwable);
            } finally {
                leaseBroker.returnLease(lease);
            }
        });
    }
}
