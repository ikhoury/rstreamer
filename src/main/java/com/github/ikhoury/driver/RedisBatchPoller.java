package com.github.ikhoury.driver;

import java.util.List;
import java.util.Optional;

public interface RedisBatchPoller {

    /**
     * Attempts to fetch a single item from the queue.
     * @param queue The queue to poll.
     * @return A an item if present.
     */
    Optional<String> pollForSingleItemFrom(String queue);

    /**
     * Attempts to fetch a batch of items from the queue.
     * @param queue The queue to poll.
     * @param count The number of items to fetch.
     * @return A list of items from the queue.
     */
    List<String> pollForMultipleItemsFrom(String queue, int count);
}
