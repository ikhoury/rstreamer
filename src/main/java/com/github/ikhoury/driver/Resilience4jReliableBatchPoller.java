package com.github.ikhoury.driver;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public class Resilience4jReliableBatchPoller extends ReliableBatchPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resilience4jReliableBatchPoller.class);
    private static final float TOLERANCE_PERCENTAGE_OF_CONNECTION_EXCEPTIONS = 0.25F;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public Resilience4jReliableBatchPoller(RedisBatchPoller poller) {
        super(poller);
        this.circuitBreaker = CircuitBreaker.of(this.getClass().getSimpleName(), createCircuitBreakerConfig());
        this.retry = Retry.of(this.getClass().getSimpleName(), createRetryConfig());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<String> pollForSingleItemFrom(String queue) throws RedisConnectionException {
        Supplier reliableSupplier = decorateSupplier(() -> super.pollForSingleItemFrom(queue));
        return getResultOrReturnEmpty(reliableSupplier, Optional.<String>empty());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) throws RedisConnectionException {
        Supplier reliableSupplier = decorateSupplier(() -> super.pollForMultipleItemsFrom(queue, count));
        return getResultOrReturnEmpty(reliableSupplier, emptyList());
    }

    private <T> T getResultOrReturnEmpty(Supplier<T> supplier, T emptyResult) {
        try {
            return supplier.get();
        } catch (RedisConnectionException connectionException) {
            LOGGER.warn("Failed to poll for items. Trying again", connectionException);
        } catch (CallNotPermittedException ignore) {

        }

        return emptyResult;
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
                .failureRateThreshold(TOLERANCE_PERCENTAGE_OF_CONNECTION_EXCEPTIONS)
                .ringBufferSizeInClosedState(MAX_RETRY_ATTEMPTS * 3)
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
