package com.github.ikhoury.rstreamer.worker;

public interface Worker {

    /**
     * Process a single item polled from the queue
     *
     * @param item Item to process
     */
    void processSingleItem(String item);
}
