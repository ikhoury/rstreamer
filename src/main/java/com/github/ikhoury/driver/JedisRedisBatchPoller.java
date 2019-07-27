package com.github.ikhoury.driver;

import com.github.ikhoury.config.JedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Optional;

import static com.github.ikhoury.config.JedisConfigBuilder.defaultJedisConfig;
import static java.util.Collections.emptyList;

public class JedisBatchedPoller implements BatchedPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisBatchedPoller.class);

    private final JedisPool jedisPool;
    private final int pollTimeoutInSeconds;

    public JedisBatchedPoller() {
        JedisConfig config = defaultJedisConfig().build();
        this.jedisPool = new JedisPool(config.getHost(), config.getPort());
        this.pollTimeoutInSeconds = config.getPollTimeoutInSeconds();
    }

    public JedisBatchedPoller(JedisConfig jedisConfig) {
        this.jedisPool = new JedisPool(jedisConfig.getHost(), jedisConfig.getPort());
        this.pollTimeoutInSeconds = jedisConfig.getPollTimeoutInSeconds();
    }

    @Override
    public Optional<String> pollForSingleItemFrom(String queue) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> pollResult = jedis.blpop(pollTimeoutInSeconds, queue);
            if (pollResult != null) {
                String item = pollResult.get(1);
                LOGGER.info("Polled one item from {}", queue);
                return Optional.of(item);
            } else {
                LOGGER.trace("No item found during last poll");
                return Optional.empty();
            }
        }
    }

    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            Response<List<String>> items = transaction.lrange(queue, 0, count - 1);
            transaction.ltrim(queue, count, -1);
            transaction.exec();
            return items.get();
        }
    }
}
