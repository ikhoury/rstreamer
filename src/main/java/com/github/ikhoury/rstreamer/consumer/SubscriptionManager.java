package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.config.subsription.LeaseConfig;
import com.github.ikhoury.rstreamer.config.subsription.PollingConfig;
import com.github.ikhoury.rstreamer.config.subsription.SubscriptionManagerConfig;
import com.github.ikhoury.rstreamer.driver.RedisBatchPoller;
import com.github.ikhoury.rstreamer.lease.LeaseBroker;
import com.github.ikhoury.rstreamer.lease.LeaseRunner;
import com.github.ikhoury.rstreamer.worker.WorkSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * This class hold all subscriptions that need to be run for data processing.
 * It must be instantiated once with all the subscriptions that must be activated.
 * After activation, data will be consumed from the queue and processed by interested workers.
 */
public class SubscriptionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);

    private final RedisBatchPoller poller;
    private final PollingConfig pollingConfig;
    private final LeaseConfig leaseConfig;

    private final Collection<WorkSubscription> subscriptions;
    private final Collection<SubscriptionRunner> subscriptionRunners;

    public SubscriptionManager(SubscriptionManagerConfig config, RedisBatchPoller poller) {
        this.subscriptionRunners = new ArrayList<>();
        this.subscriptions = new ArrayList<>();
        this.poller = poller;
        this.pollingConfig = config.getPollingConfig();
        this.leaseConfig = config.getLeaseConfig();
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
        CompletableFuture[] deactivationTasks = subscriptionRunners.stream()
                .map(subscriptionRunner -> CompletableFuture.runAsync(subscriptionRunner::stop))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(deactivationTasks).join();
    }

    private void activateSubscription(WorkSubscription subscription) {
        ExecutorService executorService = newFixedThreadPool(leaseConfig.getMaxActiveLeases());
        String queue = subscription.getQueue();
        LeaseBroker leaseBroker = new LeaseBroker(leaseConfig, queue);
        LeaseRunner leaseRunner = new LeaseRunner(leaseBroker, executorService, queue);

        PollingRoutine routine = new PollingRoutine(pollingConfig, poller, queue);
        SubscriptionRunner subscriptionRunner = new SubscriptionRunner(subscription, leaseBroker, leaseRunner, routine);
        subscriptionRunner.start();

        subscriptionRunners.add(subscriptionRunner);
    }
}