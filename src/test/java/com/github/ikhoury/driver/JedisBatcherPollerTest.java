package com.github.ikhoury.driver;

import com.github.ikhoury.config.JedisConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.ikhoury.config.JedisConfigBuilder.defaultJedisConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class JedisBatcherPollerTest {

    private static final String ITEM_QUEUE = JedisBatcherPollerTest.class.getCanonicalName() + ":" + "items";
    private static final int POLL_TIME_IN_SECONDS = 1;
    private static final String SINGLE_ITEM = "My Item";
    private static final String[] MULTIPLE_ITEMS = new String[]{"Item 1", "Item 2", "Item 3"};

    @Rule
    public GenericContainer redis = new GenericContainer<>("redis:5.0.3-alpine")
            .withExposedPorts(6379);

    private JedisRedisBatchPoller poller;
    private Jedis redisTestDriver;

    @Before
    public void setUp() {
        String host = redis.getContainerIpAddress();
        int port = redis.getFirstMappedPort();
        JedisConfig jedisConfig = defaultJedisConfig()
                .withHost(host)
                .withPort(port)
                .withPollTimeoutInSeconds(POLL_TIME_IN_SECONDS)
                .build();
        poller = new JedisRedisBatchPoller(jedisConfig);
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

        List<String> items = poller.pollForMultipleItemsFrom(ITEM_QUEUE, MULTIPLE_ITEMS.length);

        assertThat(items, containsInAnyOrder(MULTIPLE_ITEMS));
    }

    @Test
    public void noItemsRemainInQueueAfterMultiplePoll() {
        pushItemsToQueue(MULTIPLE_ITEMS);
        poller.pollForMultipleItemsFrom(ITEM_QUEUE, MULTIPLE_ITEMS.length);

        List<String> items = poller.pollForMultipleItemsFrom(ITEM_QUEUE, 1);

        assertThat(items, is(empty()));
    }

    @Test
    public void pollsForSubsetOfMultipleItems() {
        pushItemsToQueue(MULTIPLE_ITEMS);
        int numberOfItems = MULTIPLE_ITEMS.length;
        String[] expectedSubset = Arrays.copyOfRange(MULTIPLE_ITEMS, 0, numberOfItems - 1);
        String lastItem = MULTIPLE_ITEMS[numberOfItems - 1];

        List<String> subset = poller.pollForMultipleItemsFrom(ITEM_QUEUE, numberOfItems - 1);
        List<String> remaining = poller.pollForMultipleItemsFrom(ITEM_QUEUE, 1);

        assertThat(subset, containsInAnyOrder(expectedSubset));
        assertThat(remaining, containsInAnyOrder(lastItem));
    }

    private void pushItemsToQueue(String... items) {
        redisTestDriver.rpush(ITEM_QUEUE, items);
    }
}