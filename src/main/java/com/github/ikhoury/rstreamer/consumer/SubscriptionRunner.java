package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.lease.Lease;
import com.github.ikhoury.rstreamer.lease.LeaseBroker;
import com.github.ikhoury.rstreamer.lease.LeaseRunner;
import com.github.ikhoury.rstreamer.worker.BatchWorker;
import com.github.ikhoury.rstreamer.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Each {@code WorkSubscription} will have a {@code PollingThread} to service it.
 * The subscription is run by a {@code PollingRoutine} which is executed by this thread.
 */
class SubscriptionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRunner.class);
    private static final int HOLD_BACK_SECONDS_ON_EMPTY_RESULT = 1;

    private final WorkSubscription subscription;
    private final LeaseBroker leaseBroker;
    private final LeaseRunner leaseRunner;
    private final Thread nativeThread;

    SubscriptionRunner(WorkSubscription subscription, LeaseBroker leaseBroker, LeaseRunner leaseRunner, PollingRoutine routine) {
        this.leaseBroker = leaseBroker;
        this.leaseRunner = leaseRunner;
        this.subscription = subscription;
        this.nativeThread = new Thread(new SubscriptionRunnerTask(routine));

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

    private class SubscriptionRunnerTask implements Runnable {

        private final PollingRoutine routine;

        SubscriptionRunnerTask(PollingRoutine routine) {
            this.routine = routine;
        }

        @Override
        public void run() {
            String queue = subscription.getQueue();
            LOGGER.debug("Running subscription for {}", queue);

            while (!Thread.interrupted()) {
                List<String> items = routine.doPoll();

                if (!items.isEmpty()) {
                    Lease lease = leaseBroker.acquireLease();

                    if (items.size() == 1) {
                        lease.setTask(() -> processSingleItem(items.get(0)));
                    } else {
                        lease.setTask(() -> processMultipleItems(items));
                    }

                    leaseRunner.run(lease);
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(HOLD_BACK_SECONDS_ON_EMPTY_RESULT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
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
