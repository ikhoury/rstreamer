package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.config.subsription.PollingConfig;
import com.github.ikhoury.rstreamer.driver.RedisBatchPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static com.github.ikhoury.rstreamer.util.RandomOutcome.randomBooleanOutcome;
import static java.util.Collections.emptyList;

/**
 * This routine fetches work items from a queue. The routine will attempt to batch poll for items
 * to increase throughput. If there are not enough items in the queue to justify the continuous batch polling,
 * the routine switches back to single polling since it is more efficient for few items.
 */
class PollingRoutine {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingRoutine.class);

    private final RedisBatchPoller poller;
    private final String workQueue;
    private final int batchSize;
    private final int batchSizeThreshold;

    private boolean shouldBatchPoll;

    PollingRoutine(PollingConfig pollingConfig, RedisBatchPoller poller, String workQueue) {
        this.batchSize = pollingConfig.getBatchSize();
        this.batchSizeThreshold = pollingConfig.getBatchSizeThreshold();
        this.workQueue = workQueue;
        this.poller = poller;
    }

    List<String> doPoll() {
        if (shouldBatchPoll) {
            List<String> items = poller.pollForMultipleItemsFrom(workQueue, batchSize);
            LOGGER.trace("Batch polled from {} for {} and got {}", workQueue, batchSize, items.size());
            shouldBatchPoll = shouldBatchPollBasedOnSizeOf(items);
            return items;
        }

        shouldBatchPoll = shouldBatchPollBasedOnRandomOutcome();
        LOGGER.trace("Single polling from {}", workQueue);
        return poller.pollForSingleItemFrom(workQueue)
                .map(Collections::singletonList)
                .orElse(emptyList());
    }

    private boolean shouldBatchPollBasedOnSizeOf(List<String> items) {
        return items.size() >= batchSizeThreshold;
    }

    private boolean shouldBatchPollBasedOnRandomOutcome() {
        return batchSize > 1 && randomBooleanOutcome();
    }
}
