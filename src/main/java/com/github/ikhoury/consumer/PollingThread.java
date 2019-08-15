package com.github.ikhoury.consumer;

import com.github.ikhoury.lease.Lease;
import com.github.ikhoury.lease.LeaseBroker;
import com.github.ikhoury.lease.LeaseRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each {@code WorkSubscription} will have a {@code PollingThread} to service it.
 * The subscription is run by a {@code PollingRoutine} which is executed by this thread.
 */
class PollingThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingThread.class);

    private final LeaseBroker leaseBroker;
    private final LeaseRunner leaseRunner;
    private final Thread nativeThread;

    PollingThread(LeaseBroker leaseBroker, LeaseRunner leaseRunner, PollingRoutine routine) {
        this.leaseBroker = leaseBroker;
        this.leaseRunner = leaseRunner;
        this.nativeThread = new Thread(new PollingRoutineRunner(routine));

        nativeThread.setName("PollingThread-" + nativeThread.getId());
    }

    void start() {
        this.nativeThread.start();
    }

    void stop() {
        this.nativeThread.interrupt();
        try {
            this.nativeThread.join();
        } catch (InterruptedException exc) {
            LOGGER.error("{} was interrupted", nativeThread.getName(), exc);
            Thread.currentThread().interrupt();
        }
    }

    private class PollingRoutineRunner implements Runnable {

        private final PollingRoutine routine;

        PollingRoutineRunner(PollingRoutine routine) {
            this.routine = routine;
        }

        @Override
        public void run() {
            String queue = routine.getWorkQueue();
            LOGGER.debug("Running subscription for {}", queue);

            while (!Thread.interrupted()) {
                Lease lease = leaseBroker.acquireLeaseFor(routine);
                leaseRunner.run(lease);
                leaseBroker.returnLease(lease);
            }

            LOGGER.debug("Closed subscription for {}", queue);
        }
    }
}
