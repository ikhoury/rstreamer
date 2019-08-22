package com.github.ikhoury.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs leases asynchronously.
 */
public class LeaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaseRunner.class);
    private static final int SHUTDOWN_WAIT_SECONDS = 10;

    private final ExecutorService executor;
    private final LeaseBroker leaseBroker;

    public LeaseRunner(LeaseBroker leaseBroker, ExecutorService executor) {
        this.executor = executor;
        this.leaseBroker = leaseBroker;
    }

    /**
     * Every lease holds a task to be run. The task is run asynchronously.
     * The lease is automatically returned after the task completes.
     *
     * @param lease
     */
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

    public void shutdown() {
        boolean isTerminated;
        executor.shutdown();

        try {
            do {
                LOGGER.debug("Waiting for {} task(s) to complete", leaseBroker.activeLeaseCount());
                isTerminated = executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS);
            } while (!isTerminated);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to shutdown gracefully", e);
            Thread.currentThread().interrupt();
        }
    }
}
