package com.github.ikhoury.consumer;

import com.github.ikhoury.config.subsription.PollingConfig;
import com.github.ikhoury.driver.RedisBatchPoller;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

public class PollingRoutineTest {

    private static final String WORK_QUEUE = "workQueue";
    private static final String ITEM_1 = "item 1";
    private static final String ITEM_2 = "item 2";
    private static final List<String> ITEMS = asList(ITEM_1, ITEM_2);
    private static final int BATCH_SIZE = 100;
    private static final int NUMBER_OF_SPINS = 500;

    private PollingRoutine routine;
    private RedisBatchPoller poller;

    @Before
    public void setUp() {
        poller = mock(RedisBatchPoller.class);
        PollingConfig pollingConfig = mock(PollingConfig.class);

        when(pollingConfig.getBatchSize()).thenReturn(BATCH_SIZE);
        when(poller.pollForSingleItemFrom(WORK_QUEUE)).thenReturn(Optional.of(ITEM_1));
        when(poller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_SIZE)).thenReturn(ITEMS);

        routine = new PollingRoutine(pollingConfig, poller, WORK_QUEUE);
    }

    @Test
    public void attemptsToSinglePollOnFirstRun() {
        List<String> items = routine.doPoll();

        verify(poller, only()).pollForSingleItemFrom(WORK_QUEUE);
        assertThat(items, contains(ITEM_1));
    }

    @Test
    public void eventuallyAttemptsToBatchPoll() {
        attemptBatchPoll();

        verify(poller, atLeastOnce()).pollForMultipleItemsFrom(WORK_QUEUE, BATCH_SIZE);
    }

    private void attemptBatchPoll() {
        for (int i = 0; i < NUMBER_OF_SPINS; i++) {
            routine.doPoll();
        }
    }
}