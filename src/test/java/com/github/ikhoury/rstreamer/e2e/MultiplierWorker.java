package com.github.ikhoury.rstreamer.e2e;

import com.github.ikhoury.rstreamer.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static java.lang.Integer.parseInt;

/**
 * This worker is responsible for multiplying the input number
 * by the given multiplier and output the result to a queue.
 */
public class MultiplierWorker implements Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiplierWorker.class);

    private final JedisPool jedisPool;
    private final String resultQueue;
    private final int multiplier;

    public MultiplierWorker(JedisPool jedisPool, String resultQueue, int multiplier) {
        this.jedisPool = jedisPool;
        this.resultQueue = resultQueue;
        this.multiplier = multiplier;
    }

    @Override
    public void processSingleItem(String item) {
        try {
            int input = parseInt(item);

            int result = input * multiplier;

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.rpush(resultQueue, Integer.toString(result));
            }

            LOGGER.info("Processed {}", item);
        } catch (Exception e) {
            LOGGER.error("Failed to multiple {}", item, e);
        }
    }
}
