package com.github.ikhoury.consumer;

import com.github.ikhoury.driver.RedisBatchPoller;
import com.github.ikhoury.lease.LeaseBroker;
import com.github.ikhoury.lease.LeaseRunner;
import com.github.ikhoury.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.concurrent.Executors.newCachedThreadPool;

/**
 * This class hold all subscriptions that need to be run for data processing.
 * It must be instantiated once with all the subscriptions that must be activated.
 * After activation, data will be consumed from the queue and processed by interested workers.
 */
public class SubscriptionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);
    private static final int BATCH_SIZE = 100;

    private RedisBatchPoller poller;
    private Collection<WorkSubscription> subscriptions;
    private LeaseBroker leaseBroker;
    private LeaseRunner leaseRunner;

    private Collection<PollingThread> pollingThreads;

    public SubscriptionManager(RedisBatchPoller poller, Collection<WorkSubscription> subscriptions) {
        this.poller = poller;
        this.subscriptions = subscriptions;
        this.pollingThreads = new ArrayList<>(subscriptions.size());
        this.leaseBroker = new LeaseBroker(subscriptions.size());
        this.leaseRunner = new LeaseRunner(leaseBroker, newCachedThreadPool());
    }

    public void activateSubscriptions() {
        subscriptions.forEach(this::activateSubscription);
        LOGGER.info("Activated {} subscriptions", subscriptions.size());
    }

    public void deactivateSubscriptions() {
        LOGGER.info("Deactivating {} subscriptions", subscriptions.size());
        pollingThreads.forEach(PollingThread::stop);
    }

    private void activateSubscription(WorkSubscription subscription) {
        PollingRoutine routine = new PollingRoutine(poller, subscription, BATCH_SIZE);
        PollingThread pollingThread = new PollingThread(leaseBroker, leaseRunner, routine);
        pollingThread.start();
        pollingThreads.add(pollingThread);
    }
}
