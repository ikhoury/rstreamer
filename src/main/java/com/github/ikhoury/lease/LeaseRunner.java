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

    public LeaseRunner(Executor executor) {
        this.executor = executor;
    }

    public void run(Lease lease) {
        LOGGER.info("Running lease for {}", lease.getName());
        executor.execute(lease.getTask());
    }
}
