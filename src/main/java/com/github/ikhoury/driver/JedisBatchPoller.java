package com.github.ikhoury.driver;

import com.github.ikhoury.config.JedisConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Optional;

public class JedisBatchPoller implements RedisBatchPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisBatchPoller.class);

    private final JedisPool jedisPool;
    private final int pollTimeoutInSeconds;

    public JedisBatchPoller(JedisConfig jedisConfig) {
        GenericObjectPoolConfig config = configurePool(jedisConfig.getSubscriptionCount());
        this.jedisPool = new JedisPool(config, jedisConfig.getHost(), jedisConfig.getPort());
        this.pollTimeoutInSeconds = jedisConfig.getPollTimeoutInSeconds();
    }

    @Override
    public Optional<String> pollForSingleItemFrom(String queue) throws RedisConnectionException {
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
        } catch (JedisConnectionException connectionException) {
            throw new RedisConnectionException(connectionException);
        }
    }

    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) throws RedisConnectionException {
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            Response<List<String>> items = transaction.lrange(queue, 0, count - 1);
            transaction.ltrim(queue, count, -1);
            transaction.exec();

            LOGGER.info("Polled {} items from {}", items.get().size(), queue);
            return items.get();
        } catch (JedisConnectionException connectionException) {
            throw new RedisConnectionException(connectionException);
        }
    }

    /**
     * We need as much connections as the number of subscriptions, since
     * each subscription is backed by one polling thread.
     *
     * @param subscriptionCount The number of subscriptions that will be run
     * @return Configuration for the jedis pool
     */
    private GenericObjectPoolConfig configurePool(int subscriptionCount) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(subscriptionCount);
        config.setMaxIdle(subscriptionCount);
        config.setJmxEnabled(false);
        return config;
    }
}
