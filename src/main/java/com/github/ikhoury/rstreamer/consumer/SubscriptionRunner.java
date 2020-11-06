package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.lease.LeaseRunner;
import com.github.ikhoury.rstreamer.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each {@code WorkSubscription} will have a polling thread to service it.
 * The subscription is run by a {@code PollingRoutine} which is executed by this thread.
 */
class SubscriptionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRunner.class);

    private final WorkSubscription subscription;
    private final LeaseRunner leaseRunner;
    private final Thread pollingThread;

    SubscriptionRunner(
            WorkSubscription subscription,
            LeaseRunner leaseRunner,
            PollingRoutine pollingRoutine,
            WorkRoutine workRoutine
    ) {
        this.leaseRunner = leaseRunner;
        this.subscription = subscription;
        this.pollingThread = new Thread(new SubscriptionRunnerTask(pollingRoutine, workRoutine));

        pollingThread.setName("SubscriptionRunner-" + pollingThread.getId());
    }

    void start() {
        this.pollingThread.start();
    }

    void stop() {
        this.pollingThread.interrupt();
        try {
            this.pollingThread.join();
            this.leaseRunner.shutdown();
        } catch (InterruptedException exc) {
            LOGGER.error("{} was interrupted and failed to shutdown gracefully", pollingThread.getName(), exc);
            Thread.currentThread().interrupt();
        }
    }

    private class SubscriptionRunnerTask implements Runnable {

        private final PollingRoutine pollingRoutine;
        private final WorkRoutine workRoutine;

        SubscriptionRunnerTask(PollingRoutine pollingRoutine, WorkRoutine workRoutine) {
            this.pollingRoutine = pollingRoutine;
            this.workRoutine = workRoutine;
        }

        @Override
        public void run() {
            var queue = subscription.getQueue();
            LOGGER.debug("Running subscription for {}", queue);

            while (!Thread.interrupted()) {
                var items = pollingRoutine.doPoll();
                workRoutine.process(items);
            }

            LOGGER.debug("Closed subscription for {}", queue);
        }
    }
}
