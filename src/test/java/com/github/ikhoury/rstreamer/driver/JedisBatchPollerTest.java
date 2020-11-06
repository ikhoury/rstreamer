package com.github.ikhoury.rstreamer.driver;

import com.github.ikhoury.rstreamer.config.poller.BatchPollerConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.ikhoury.rstreamer.config.poller.BatchPollerConfigBuilder.defaultBatchPollerConfig;
import static com.github.ikhoury.rstreamer.util.Container.REDIS;
import static com.github.ikhoury.rstreamer.util.TimeInterval.SHORT_SECOND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class JedisBatchPollerTest {

    private static final String ITEM_QUEUE = JedisBatchPollerTest.class.getCanonicalName() + ":" + "items";
    private static final String SINGLE_ITEM = "My Item";
    private static final String[] MULTIPLE_ITEMS = new String[]{"Item 1", "Item 2", "Item 3"};
    private static final int NUMBER_OF_MULTIPLE_ITEMS = MULTIPLE_ITEMS.length;

    @Rule
    public final GenericContainer redis = new GenericContainer<>(REDIS)
            .withExposedPorts(6379);

    private JedisBatchPoller poller;
    private Jedis redisTestDriver;

    @Before
    public void setUp() {
        String host = redis.getContainerIpAddress();
        int port = redis.getFirstMappedPort();
        int subscriptionCount = 1;
        BatchPollerConfig batchPollerConfig = defaultBatchPollerConfig()
                .withHost(host)
                .withPort(port)
                .withPollTimeoutInSeconds(SHORT_SECOND)
                .build();
        poller = new JedisBatchPoller(batchPollerConfig, subscriptionCount);
        redisTestDriver = new Jedis(host, port);
    }

    @Test
    public void pollsSingleItem() {
        pushItemsToQueue(SINGLE_ITEM);

        Optional<String> itemOpt = poller.pollForSingleItemFrom(ITEM_QUEUE);
        if (itemOpt.isPresent()) {
            assertThat(itemOpt.get(), equalTo(SINGLE_ITEM));
        } else {
            fail("No item was polled from the queue.");
        }
    }

    @Test
    public void noItemsRemainInQueueAfterSinglePoll() {
        pushItemsToQueue(SINGLE_ITEM);
        poller.pollForSingleItemFrom(ITEM_QUEUE);

        Optional<String> itemOpt = poller.pollForSingleItemFrom(ITEM_QUEUE);

        assertThat(itemOpt.isPresent(), equalTo(false));
    }

    @Test
    public void pollsMultipleItems() {
        pushItemsToQueue(MULTIPLE_ITEMS);

        List<String> items = poller.pollForMultipleItemsFrom(ITEM_QUEUE, NUMBER_OF_MULTIPLE_ITEMS);

        assertThat(items, containsInAnyOrder(MULTIPLE_ITEMS));
    }

    @Test
    public void pollsForSubsetOfMultipleItems() {
        pushItemsToQueue(MULTIPLE_ITEMS);
        String[] expectedSubset = Arrays.copyOfRange(MULTIPLE_ITEMS, 0, NUMBER_OF_MULTIPLE_ITEMS - 1);
        String lastItem = MULTIPLE_ITEMS[NUMBER_OF_MULTIPLE_ITEMS - 1];

        List<String> subset = poller.pollForMultipleItemsFrom(ITEM_QUEUE, NUMBER_OF_MULTIPLE_ITEMS - 1);
        List<String> remaining = poller.pollForMultipleItemsFrom(ITEM_QUEUE, 1);

        assertThat(subset, containsInAnyOrder(expectedSubset));
        assertThat(remaining, containsInAnyOrder(lastItem));
    }

    @Test(expected = RedisConnectionException.class)
    public void throwsRedisConnectionExceptionOnSinglePoll() {
        redis.stop();

        poller.pollForSingleItemFrom(ITEM_QUEUE);
    }

    @Test(expected = RedisConnectionException.class)
    public void throwsRedisConnectionExceptionOnBatchPoll() {
        redis.stop();

        poller.pollForMultipleItemsFrom(ITEM_QUEUE, NUMBER_OF_MULTIPLE_ITEMS);
    }

    private void pushItemsToQueue(String... items) {
        redisTestDriver.rpush(ITEM_QUEUE, items);
    }
}