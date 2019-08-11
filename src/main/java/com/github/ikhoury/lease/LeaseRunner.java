package com.github.ikhoury.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Runs leases asynchronously.
 */
public class LeaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaseRunner.class);

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    public static void run(Lease lease) {
        LOGGER.info("Running lease for {}", lease.getName());
        EXECUTOR.execute(lease.getTask());
    }
}
