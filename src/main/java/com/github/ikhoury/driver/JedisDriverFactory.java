package com.github.ikhoury.driver;

import com.github.ikhoury.config.RedisConfig;
import redis.clients.jedis.JedisPool;

public class JedisDriverFactory {
    private static final RedisConfig DEFAULT_CONFIG = new RedisConfig();

    private final JedisPool jedisPool;

    public JedisDriverFactory() {
        this.jedisPool = new JedisPool(DEFAULT_CONFIG.getHost(), DEFAULT_CONFIG.getPort());
    }

    public JedisDriverFactory(RedisConfig redisConfig) {
        this.jedisPool = new JedisPool(redisConfig.getHost(), redisConfig.getPort());
    }
}
