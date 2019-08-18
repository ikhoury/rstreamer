package com.github.ikhoury.consumer;

import com.github.ikhoury.config.PollingConfig;
import com.github.ikhoury.config.RStreamerConfig;
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

    private RedisBatchPoller poller;
    private LeaseBroker leaseBroker;
    private LeaseRunner leaseRunner;
    private PollingConfig pollingConfig;

    private Collection<WorkSubscription> subscriptions;
    private Collection<PollingThread> pollingThreads;

    public SubscriptionManager(RStreamerConfig rStreamerConfig, RedisBatchPoller poller) {
        this.pollingThreads = new ArrayList<>();
        this.subscriptions = new ArrayList<>();
        this.poller = poller;
        this.pollingConfig = rStreamerConfig.getPollingConfig();
        this.leaseBroker = new LeaseBroker(rStreamerConfig.getLeaseConfig());
        this.leaseRunner = new LeaseRunner(leaseBroker, newCachedThreadPool());
    }

    public void addSubscription(WorkSubscription subscription) {
        this.subscriptions.add(subscription);
    }

    public void addSubscriptions(Collection<WorkSubscription> subscriptions) {
        this.subscriptions.addAll(subscriptions);
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
        PollingRoutine routine = new PollingRoutine(pollingConfig, poller, subscription);
        PollingThread pollingThread = new PollingThread(leaseBroker, leaseRunner, routine);
        pollingThread.start();
        pollingThreads.add(pollingThread);
    }
}
