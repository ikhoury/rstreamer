package com.github.ikhoury.rstreamer.driver;

import java.util.List;
import java.util.Optional;

public abstract class ReliableBatchPoller implements RedisBatchPoller {

    private final RedisBatchPoller poller;

    ReliableBatchPoller(RedisBatchPoller poller) {
        this.poller = poller;
    }

    @Override
    public Optional<String> pollForSingleItemFrom(String queue) {
        return poller.pollForSingleItemFrom(queue);
    }

    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) {
        return poller.pollForMultipleItemsFrom(queue, count);
    }
}
