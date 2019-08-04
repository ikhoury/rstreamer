package com.github.ikhoury.dispatcher;

import com.github.ikhoury.driver.RedisBatchPoller;
import com.github.ikhoury.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class JobManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    private static final int BATCH_SIZE = 100;
    private static final int THREAD_WAIT_TIME_IN_MILLIS = 5000;

    private RedisBatchPoller poller;
    private Collection<WorkSubscription> subscriptions;

    private Collection<PollingThread> pollingThreads;

    public JobManager(RedisBatchPoller poller, Collection<WorkSubscription> subscriptions) {
        this.poller = poller;
        this.subscriptions = subscriptions;
        this.pollingThreads = new ArrayList<>(subscriptions.size());
    }

    public void activateSubscriptions() {
        subscriptions.forEach(this::activateSubscription);
        LOGGER.info("Activated {} subscriptions", subscriptions.size());
    }

    public void deactivateSubscriptions() {
        pollingThreads.forEach(PollingThread::interrupt);
        pollingThreads.forEach(pollingThread -> {
            try {
                pollingThread.join(THREAD_WAIT_TIME_IN_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.error("Polling thread interrupted", e);
            }

            if (pollingThread.isAlive()) {
                LOGGER.warn("{} did not finish on time", pollingThread.getName());
            }
        });
        pollingThreads.clear();
    }

    private void activateSubscription(WorkSubscription subscription) {
        PollingThread pollingThread = new PollingThread(new PollingRoutine(poller, subscription, BATCH_SIZE));
        pollingThread.start();
        pollingThreads.add(pollingThread);
    }
}
