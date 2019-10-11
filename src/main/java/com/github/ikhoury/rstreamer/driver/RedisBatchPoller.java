package com.github.ikhoury.rstreamer.driver;

import java.util.List;
import java.util.Optional;

public interface RedisBatchPoller {

    /**
     * Attempts to fetch a single item from the queue.
     * @param queue The queue to poll.
     * @return An item if present.
     * @throws RedisConnectionException When a connection error with redis occurs
     */
    Optional<String> pollForSingleItemFrom(String queue) throws RedisConnectionException;

    /**
     * Attempts to fetch a batch of items from the queue.
     * @param queue The queue to poll.
     * @param count The number of items to fetch.
     * @return A list of items from the queue.
     * @throws RedisConnectionException When a connection error with redis occurs
     */
    List<String> pollForMultipleItemsFrom(String queue, int count) throws RedisConnectionException;
}
