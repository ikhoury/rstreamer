package com.github.ikhoury.dispatcher;

import com.github.ikhoury.driver.RedisBatchPoller;
import com.github.ikhoury.worker.WorkSubscription;
import com.github.ikhoury.worker.Worker;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class JobManagerTest {

    private static final String ITEM = "item";
    private static final String QUEUE = "queue";

    private JobManager manager;
    private WorkSubscription subscription;
    private Worker worker;
    private RedisBatchPoller poller;

    @Before
    public void setUp() {
        worker = mock(Worker.class);
        subscription = new WorkSubscription(QUEUE, singleton(worker));
        poller = mock(RedisBatchPoller.class);
        when(poller.pollForSingleItemFrom(QUEUE)).thenReturn(Optional.of(ITEM));
        when(poller.pollForMultipleItemsFrom(eq(QUEUE), anyInt())).thenReturn(singletonList(ITEM));

        manager = new JobManager(poller, singleton(subscription));
        manager.activateSubscriptions();
    }

    @Test
    public void activatesRegisteredSubscriptions() {
        verify(poller, atLeastOnce()).pollForSingleItemFrom(QUEUE);
        verify(worker, atLeastOnce()).processSingleItem(ITEM);
    }

    @Test
    public void deactivatesRegisteredSubscriptions() {
        manager.deactivateSubscriptions();
        reset(poller, worker);

        verifyNoMoreInteractions(poller, worker);
    }
}