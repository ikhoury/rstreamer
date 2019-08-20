package com.github.ikhoury.consumer;

import com.github.ikhoury.lease.Lease;
import com.github.ikhoury.lease.LeaseBroker;
import com.github.ikhoury.lease.LeaseRunner;
import com.github.ikhoury.worker.BatchWorker;
import com.github.ikhoury.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Each {@code WorkSubscription} will have a {@code PollingThread} to service it.
 * The subscription is run by a {@code PollingRoutine} which is executed by this thread.
 */
class SubscriptionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRunner.class);

    private final WorkSubscription subscription;
    private final LeaseBroker leaseBroker;
    private final LeaseRunner leaseRunner;
    private final Thread nativeThread;

    SubscriptionRunner(WorkSubscription subscription, LeaseBroker leaseBroker, LeaseRunner leaseRunner, PollingRoutine routine) {
        this.leaseBroker = leaseBroker;
        this.leaseRunner = leaseRunner;
        this.subscription = subscription;
        this.nativeThread = new Thread(new SubscriptionRunnerRoutine(routine));

        nativeThread.setName("SubscriptionRunner-" + nativeThread.getId());
    }

    void start() {
        this.nativeThread.start();
    }

    void stop() {
        this.nativeThread.interrupt();
        try {
            this.nativeThread.join();
            this.leaseRunner.shutdown();
        } catch (InterruptedException exc) {
            LOGGER.error("{} was interrupted and failed to shutdown gracefully", nativeThread.getName(), exc);
            Thread.currentThread().interrupt();
        }
    }

    private class SubscriptionRunnerRoutine implements Runnable {

        private final PollingRoutine routine;

        SubscriptionRunnerRoutine(PollingRoutine routine) {
            this.routine = routine;
        }

        @Override
        public void run() {
            String queue = subscription.getQueue();
            LOGGER.debug("Running subscription for {}", queue);

            while (!Thread.interrupted()) {
                List<String> items = routine.doPoll();

                if (!items.isEmpty()) {
                    Lease lease = leaseBroker.acquireLeaseFor(queue);

                    if (items.size() == 1) {
                        lease.setTask(() -> processSingleItem(items.get(0)));
                    } else {
                        lease.setTask(() -> processMultipleItems(items));
                    }

                    leaseRunner.run(lease);
                }
            }

            LOGGER.debug("Closed subscription for {}", queue);
        }

        private void processSingleItem(String item) {
            subscription.getWorkers().forEach(worker -> {
                LOGGER.trace("Worker {} processing single item", worker.getClass().getCanonicalName());
                worker.processSingleItem(item);
            });
        }

        private void processMultipleItems(List<String> items) {
            subscription.getWorkers().forEach(worker -> {
                if (worker instanceof BatchWorker) {
                    LOGGER.trace("Worker {} processing {} items", worker.getClass().getCanonicalName(), items.size());
                    ((BatchWorker) worker).processMultipleItems(items);
                } else {
                    LOGGER.trace("Worker {} processing single item", worker.getClass().getCanonicalName());
                    items.forEach(worker::processSingleItem);
                }
            });
        }
    }
}
