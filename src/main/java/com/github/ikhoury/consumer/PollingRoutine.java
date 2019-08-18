package com.github.ikhoury.consumer;

import com.github.ikhoury.config.PollingConfig;
import com.github.ikhoury.driver.RedisBatchPoller;
import com.github.ikhoury.worker.BatchWorker;
import com.github.ikhoury.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.github.ikhoury.util.RandomOutcome.randomBooleanOutcome;

/**
 * This routine fetches work items from a queue and processes it
 * with every worker in the subscription. The routine will attempt to batch poll for items
 * to increase throughput. If there are not enough items in the queue to justify the heavy continuous batch polling,
 * the routine switches back to single polling.
 */
public class PollingRoutine {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingRoutine.class);

    private final RedisBatchPoller poller;
    private final WorkSubscription subscription;
    private final int batchSize;
    private final int batchSizeThreshold;

    private boolean shouldBatchPoll;

    PollingRoutine(PollingConfig pollingConfig, RedisBatchPoller poller, WorkSubscription subscription) {
        this.batchSize = pollingConfig.getBatchSize();
        this.batchSizeThreshold = pollingConfig.getBatchSizeThreshold();
        this.subscription = subscription;
        this.poller = poller;
    }

    public void doPoll() {
        String queue = subscription.getQueue();

        if (shouldBatchPoll) {
            List<String> items = poller.pollForMultipleItemsFrom(queue, batchSize);
            processMultipleItems(items);
            shouldBatchPoll = shouldBatchPollBasedOnSizeOf(items);
        } else {
            poller.pollForSingleItemFrom(queue).ifPresent(this::processSingleItem);
            shouldBatchPoll = shouldBatchPollBasedOnRandomOutcome();
        }
    }

    public String getWorkQueue() {
        return subscription.getQueue();
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

    private boolean shouldBatchPollBasedOnSizeOf(List<String> items) {
        return !(items.size() < batchSizeThreshold);
    }

    private boolean shouldBatchPollBasedOnRandomOutcome() {
        return randomBooleanOutcome();
    }
}
