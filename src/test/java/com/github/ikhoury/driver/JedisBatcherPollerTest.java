package com.github.ikhoury.driver;

import com.github.ikhoury.config.RedisConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.Jedis;

import java.util.stream.IntStream;

public class JedisDriverFactoryTest {

    private static final String ITEM_QUEUE = JedisDriverFactoryTest.class.getCanonicalName() + ":" + "items";

    @Rule
    public GenericContainer redis = new GenericContainer<>("redis:5.0.3-alpine")
            .withExposedPorts(6379);

    private JedisBatchedPoller driverFactory;
    private Jedis redisTestDriver;

    @Before
    public void setUp() {
        String host = redis.getContainerIpAddress();
        int port = redis.getFirstMappedPort();
        RedisConfig redisConfig = new RedisConfig(host, port);
        driverFactory = new JedisBatchedPoller(redisConfig);
        redisTestDriver = new Jedis(host, port);
    }

    @Test
    public void pollsSingleItem() {
        pushNumberOfItems(1);

        String item = driverFactory
    }

    private void pushNumberOfItems(int count) {
        IntStream.range(0, count)
                .forEach(item -> redisTestDriver.lpush(ITEM_QUEUE, Integer.toString(item)));
    }
}