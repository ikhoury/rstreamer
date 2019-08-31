package com.github.ikhoury.driver;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Resilience4jReliableBatchPoller extends ReliableBatchPoller {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private static final float TOLERANCE_PERCENTAGE_OF_CONNECTION_EXCEPTION = 0.25F;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    public Resilience4jReliableBatchPoller(RedisBatchPoller poller, String queue) {
        super(poller);
        this.circuitBreaker = CircuitBreaker.of(queue, createCircuitBreakerConfig());
        this.retry = Retry.of(queue, createRetryConfig());
    }

    @Override
    public Optional<String> pollForSingleItemFrom(String queue) throws RedisConnectionException {
        return decorateSupplier(() -> super.pollForSingleItemFrom(queue)).get();
    }

    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) throws RedisConnectionException {
        return decorateSupplier(() -> super.pollForMultipleItemsFrom(queue, count)).get();
    }

    private <T> Supplier<T> decorateSupplier(Supplier<T> supplier) {
        return Decorators.ofSupplier(supplier)
                .withCircuitBreaker(circuitBreaker)
                .withRetry(retry)
                .decorate();
    }

    private CircuitBreakerConfig createCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .recordExceptions(RedisConnectionException.class)
                .failureRateThreshold(TOLERANCE_PERCENTAGE_OF_CONNECTION_EXCEPTION)
                .ringBufferSizeInClosedState(MAX_RETRY_ATTEMPTS * 10)
                .ringBufferSizeInHalfOpenState(MAX_RETRY_ATTEMPTS)
                .build();
    }

    private RetryConfig createRetryConfig() {
        return RetryConfig.custom()
                .retryExceptions(RedisConnectionException.class)
                .maxAttempts(MAX_RETRY_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff())
                .build();
    }
}
