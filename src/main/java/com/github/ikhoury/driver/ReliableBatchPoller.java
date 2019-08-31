package com.github.ikhoury.driver;

import java.util.List;
import java.util.Optional;

public abstract class ReliableBatchPoller implements RedisBatchPoller {

    final RedisBatchPoller poller;

    ReliableBatchPoller(RedisBatchPoller poller) {
        this.poller = poller;
    }

    @Override
    public Optional<String> pollForSingleItemFrom(String queue) throws RedisConnectionException {
        return poller.pollForSingleItemFrom(queue);
    }

    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) throws RedisConnectionException {
        return poller.pollForMultipleItemsFrom(queue, count);
    }
}
