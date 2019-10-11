package com.github.ikhoury.rstreamer.driver;

import com.github.ikhoury.rstreamer.config.poller.ReliableBatchPollerConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.github.ikhoury.rstreamer.config.poller.ReliableBatchPollerConfigBuilder.defaultReliableBatchPollerConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

public class Resilience4jReliableBatchPollerTest {

    private static final String WORK_QUEUE = "queue";
    private static final int BATCH_POLL_SIZE = 10;
    private static final int RETRY_ATTEMPTS = 3;
    private static final int SAMPLE_COUNT_MULTIPLIER = 1;
    private static final float FAILURE_RATE_THRESHOLD = 0.1F;

    private RedisBatchPoller mockBatchPoller;
    private Resilience4jReliableBatchPoller reliableBatchPoller;

    @Before
    public void setUp() {
        mockBatchPoller = mock(RedisBatchPoller.class);
        ReliableBatchPollerConfig config = defaultReliableBatchPollerConfig()
                .withRetryAttempts(RETRY_ATTEMPTS)
                .withSampleCountMultiplier(SAMPLE_COUNT_MULTIPLIER)
                .withFailureRateThreshold(FAILURE_RATE_THRESHOLD)
                .build();
        int subscriptionCount = 1;

        reliableBatchPoller = new Resilience4jReliableBatchPoller(mockBatchPoller, config, subscriptionCount);
    }

    @Test
    public void returnsEmptyResultForSinglePollOnConnectionException() {
        when(mockBatchPoller.pollForSingleItemFrom(WORK_QUEUE)).thenThrow(RedisConnectionException.class);

        Optional<String> item = reliableBatchPoller.pollForSingleItemFrom(WORK_QUEUE);

        assertThat(item.isPresent(), equalTo(false));
    }

    @Test
    public void returnsEmptyResultForBatchPollOnConnectionException() {
        when(mockBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE)).thenThrow(RedisConnectionException.class);

        List<String> items = reliableBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);

        assertThat(items, hasSize(0));
    }

    @Test(expected = RuntimeException.class)
    public void bubblesUpOtherExceptionsForSinglePoll() {
        when(mockBatchPoller.pollForSingleItemFrom(WORK_QUEUE)).thenThrow(RuntimeException.class);

        reliableBatchPoller.pollForSingleItemFrom(WORK_QUEUE);
    }

    @Test(expected = RuntimeException.class)
    public void bubblesUpOtherExceptionsForBatchPoll() {
        when(mockBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE)).thenThrow(RuntimeException.class);

        reliableBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);
    }

    @Test
    public void retriesSinglePollOnConnectionException() {
        when(mockBatchPoller.pollForSingleItemFrom(WORK_QUEUE)).thenThrow(RedisConnectionException.class);

        reliableBatchPoller.pollForSingleItemFrom(WORK_QUEUE);

        verify(mockBatchPoller, times(RETRY_ATTEMPTS)).pollForSingleItemFrom(WORK_QUEUE);
    }

    @Test
    public void doesNotRetryOnOtherExceptionsForSinglePoll() {
        when(mockBatchPoller.pollForSingleItemFrom(WORK_QUEUE)).thenThrow(RuntimeException.class);

        try {
            reliableBatchPoller.pollForSingleItemFrom(WORK_QUEUE);
        } catch (RuntimeException ignore) {

        }

        verify(mockBatchPoller).pollForSingleItemFrom(WORK_QUEUE);
    }

    @Test
    public void retriesBatchPollOnConnectionException() {
        when(mockBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE)).thenThrow(RedisConnectionException.class);

        reliableBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);

        verify(mockBatchPoller, times(RETRY_ATTEMPTS)).pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);
    }

    @Test
    public void doesNotRetryOnOtherExceptionsForBatchPoll() {
        when(mockBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE)).thenThrow(RuntimeException.class);

        try {
            reliableBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);
        } catch (RuntimeException ignore) {

        }

        verify(mockBatchPoller).pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);
    }

    @Test
    public void stopsSinglePollingAfterFailureRateThresholdExceeded() {
        when(mockBatchPoller.pollForSingleItemFrom(WORK_QUEUE)).thenThrow(RedisConnectionException.class);

        // First attempt tries to poll
        reliableBatchPoller.pollForSingleItemFrom(WORK_QUEUE);
        verify(mockBatchPoller, times(RETRY_ATTEMPTS)).pollForSingleItemFrom(WORK_QUEUE);

        // Second attempt does not poll
        reliableBatchPoller.pollForSingleItemFrom(WORK_QUEUE);
        verifyNoMoreInteractions(mockBatchPoller);
    }

    @Test
    public void stopsBatchPollingAfterFailureRateThresholdExceeded() {
        when(mockBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE)).thenThrow(RedisConnectionException.class);

        // First attempt tries to poll
        reliableBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);
        verify(mockBatchPoller, times(RETRY_ATTEMPTS)).pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);

        // Second attempt does not poll
        reliableBatchPoller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_POLL_SIZE);
        verifyNoMoreInteractions(mockBatchPoller);
    }
}