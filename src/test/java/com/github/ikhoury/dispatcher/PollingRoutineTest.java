package com.github.ikhoury.dispatcher;

import com.github.ikhoury.driver.RedisBatchPoller;
import com.github.ikhoury.worker.BatchWorker;
import com.github.ikhoury.worker.WorkSubscription;
import com.github.ikhoury.worker.Worker;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class PollingRoutineTest {
    private static final String WORK_QUEUE = "workQueue";
    private static final String ITEM_1 = "item 1";
    private static final String ITEM_2 = "item 2";
    private static final List<String> ITEMS = asList(ITEM_1, ITEM_2);
    private static final int BATCH_SIZE = 100;
    private static final int NUMBER_OF_SPINS = 500;

    private PollingRoutine routine;
    private Worker singleItemWorker;
    private BatchWorker multipleItemsWorker;
    private RedisBatchPoller poller;

    @Before
    public void setUp() {
        WorkSubscription subscription = mock(WorkSubscription.class);
        singleItemWorker = mock(Worker.class);
        multipleItemsWorker = mock(BatchWorker.class);
        poller = mock(RedisBatchPoller.class);

        when(subscription.getQueue()).thenReturn(WORK_QUEUE);
        when(subscription.getWorkers()).thenReturn(asList(singleItemWorker, multipleItemsWorker));
        when(poller.pollForSingleItemFrom(WORK_QUEUE)).thenReturn(Optional.of(ITEM_1));
        when(poller.pollForMultipleItemsFrom(WORK_QUEUE, BATCH_SIZE)).thenReturn(ITEMS);

        routine = new PollingRoutine(poller, subscription, BATCH_SIZE);
    }

    @Test
    public void attemptsToSinglePollOnFirstRun() {
        routine.doPoll();

        verify(poller, only()).pollForSingleItemFrom(WORK_QUEUE);
    }

    @Test
    public void eventuallyAttemptsToBatchPoll() {
        attemptBatchPoll();

        verify(poller, atLeastOnce()).pollForMultipleItemsFrom(WORK_QUEUE, BATCH_SIZE);
    }

    @Test
    public void invokesWorkersToProcessSingleItem() {
        routine.doPoll();

        verify(singleItemWorker, only()).processSingleItem(ITEM_1);
        verify(multipleItemsWorker, only()).processSingleItem(ITEM_1);
    }

    @Test
    public void invokesWorkersToProcessMultipleItems() {
        attemptBatchPoll();

        verify(singleItemWorker, atLeastOnce()).processSingleItem(ITEM_1);
        verify(singleItemWorker, atLeastOnce()).processSingleItem(ITEM_2);
        verify(multipleItemsWorker, atLeastOnce()).processMultipleItems(ITEMS);
    }

    private void attemptBatchPoll() {
        for (int i = 0; i < NUMBER_OF_SPINS; i++) {
            routine.doPoll();
        }
    }
}