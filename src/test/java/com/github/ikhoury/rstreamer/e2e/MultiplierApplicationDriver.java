package com.github.ikhoury.rstreamer.e2e;

import com.github.ikhoury.rstreamer.consumer.SubscriptionManager;
import com.github.ikhoury.rstreamer.driver.JedisBatchPoller;
import com.github.ikhoury.rstreamer.driver.RedisBatchPoller;
import com.github.ikhoury.rstreamer.worker.WorkSubscription;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.ikhoury.rstreamer.config.poller.BatchPollerConfigBuilder.defaultBatchPollerConfig;
import static com.github.ikhoury.rstreamer.config.subsription.LeaseConfigBuilder.defaultLeaseConfig;
import static com.github.ikhoury.rstreamer.config.subsription.SubscriptionManageConfigBuilder.defaultSubscriptionManagerConfig;
import static java.lang.Integer.parseInt;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

/**
 * The driver is responsible for creating an instance of a multiplier application
 * using the @(class MultiplierWorker) to process input numbers.
 * The driver allows the test class to inspect the state of the application and verify its output.
 */
public class MultiplierApplicationDriver {

    private static final String INPUT_QUEUE = "numbers";
    private static final String RESULT_QUEUE = "multiplied-numbers";
    private static final int WAIT_FOR_RESULT_SECONDS = 10;
    private static final int AVAILABLE_LEASES = 10;

    private final Jedis driverJedis;
    private final SubscriptionManager subscriptionManager;

    public MultiplierApplicationDriver(String redisHost, int redisPort, int multiplier) {
        this.driverJedis = new Jedis(redisHost, redisPort);
        this.subscriptionManager = new SubscriptionManager(
                defaultSubscriptionManagerConfig()
                        .with(defaultLeaseConfig().withAvailableLeases(AVAILABLE_LEASES))
                        .build(),
                createBatchPoller(redisPort)
        );

        subscriptionManager.addSubscription(createSubscription(redisHost, redisPort, multiplier));
    }

    public void start() {
        subscriptionManager.activateSubscriptions();
    }

    public void stop() {
        subscriptionManager.deactivateSubscriptions();
    }

    public void sendNumber(int number) {
        driverJedis.rpush(INPUT_QUEUE, Integer.toString(number));
    }

    public int removeFirstOutputNumber() {
        String result = driverJedis.blpop(WAIT_FOR_RESULT_SECONDS, RESULT_QUEUE).get(1);

        return parseInt(result);
    }

    public List<Integer> currentOutputNumbers() {
        return driverJedis.lrange(RESULT_QUEUE, 0, -1)
                .stream()
                .map(Integer::parseInt)
                .collect(toList());
    }

    public void waitForInputToBeProcessed() throws InterruptedException {
        while (driverJedis.llen(INPUT_QUEUE) > 0) {
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private WorkSubscription createSubscription(String redisHost, int redisPort, int multiplier) {
        MultiplierWorker worker = new MultiplierWorker(new JedisPool(redisHost, redisPort), RESULT_QUEUE, multiplier);

        return new WorkSubscription(INPUT_QUEUE, singleton(worker));
    }

    private RedisBatchPoller createBatchPoller(int redisPort) {
        return new JedisBatchPoller(
                defaultBatchPollerConfig().withPort(redisPort).build(),
                1
        );
    }
}
