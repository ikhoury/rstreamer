package com.github.ikhoury.rstreamer.lease;

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
    private final String queue;

    public LeaseRunner(LeaseBroker leaseBroker, ExecutorService executor, String queue) {
        this.executor = executor;
        this.leaseBroker = leaseBroker;
        this.queue = queue;
    }

    /**
     * Every lease holds a task to be run. The task is run asynchronously.
     * The lease is automatically returned after the task completes.
     *
     * @param lease Lease that contains the task to run
     */
    public void run(Lease lease) {
        LOGGER.info("Running lease for {}", queue);
        executor.execute(() -> {
            try {
                lease.getTask().run();
            } catch (Exception exception) {
                LOGGER.error("Failed to run lease for {}", queue, exception);
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
                LOGGER.debug("Waiting for {} task(s) to complete for {}", leaseBroker.activeLeaseCount(), queue);
                isTerminated = executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS);
            } while (!isTerminated);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to shutdown gracefully", e);
            Thread.currentThread().interrupt();
        }
    }
}
