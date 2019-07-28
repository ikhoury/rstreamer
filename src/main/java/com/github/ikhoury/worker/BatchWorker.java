package com.github.ikhoury.worker;

import java.util.List;

public interface BatchWorker extends Worker {

    /**
     * Process multiple items polled from the queue
     *
     * @param items Items to process
     */
    void processMultipleItems(List<String> items);
}
