package com.github.ikhoury.driver;

import com.github.ikhoury.config.poller.ReliableBatchPollerConfig;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public class Resilience4jReliableBatchPoller extends ReliableBatchPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resilience4jReliableBatchPoller.class);
    private static final Duration DURATION_IN_OPEN_STATE = Duration.ofMinutes(1);

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public Resilience4jReliableBatchPoller(RedisBatchPoller poller, ReliableBatchPollerConfig config, int subscriptionCount) {
        super(poller);
        CircuitBreakerConfig circuitBreakerConfig = createCircuitBreakerConfig(subscriptionCount, config.getRetryAttempts(),
                config.getFailureRateThreshold(), config.getSampleCountMultiplier());
        RetryConfig retryConfig = createRetryConfig(config.getRetryAttempts());
        String simpleName = this.getClass().getSimpleName();
        this.circuitBreaker = CircuitBreaker.of(simpleName, circuitBreakerConfig);
        this.retry = Retry.of(simpleName, retryConfig);
    }

    @Override
    public Optional<String> pollForSingleItemFrom(String queue) throws RedisConnectionException {
        Supplier<Optional<String>> reliableSupplier = decorateSupplier(() -> super.pollForSingleItemFrom(queue));
        return getResultOrReturnEmpty(reliableSupplier, Optional.<String>empty());
    }

    @Override
    public List<String> pollForMultipleItemsFrom(String queue, int count) throws RedisConnectionException {
        Supplier<List<String>> reliableSupplier = decorateSupplier(() -> super.pollForMultipleItemsFrom(queue, count));
        return getResultOrReturnEmpty(reliableSupplier, emptyList());
    }

    private <T> T getResultOrReturnEmpty(Supplier<T> supplier, T emptyResult) {
        try {
            return supplier.get();
        } catch (RedisConnectionException connectionException) {
            LOGGER.warn("Failed to poll for items", connectionException);
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

    private CircuitBreakerConfig createCircuitBreakerConfig(int subscriptionCount, int retryAttempts,
                                                            float failureRateThreshold, int sampleCountMultiplier) {
        return CircuitBreakerConfig.custom()
                .recordExceptions(RedisConnectionException.class)
                .failureRateThreshold(failureRateThreshold)
                .slidingWindowSize(retryAttempts * subscriptionCount * sampleCountMultiplier)
                .permittedNumberOfCallsInHalfOpenState(retryAttempts * subscriptionCount)
                .waitDurationInOpenState(DURATION_IN_OPEN_STATE)
                .build();
    }

    private RetryConfig createRetryConfig(int retryAttempts) {
        return RetryConfig.custom()
                .retryExceptions(RedisConnectionException.class)
                .maxAttempts(retryAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff())
                .build();
    }
}
