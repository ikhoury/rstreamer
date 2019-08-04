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

public class SubscriptionManagerTest {

    private static final String ITEM = "item";
    private static final String QUEUE = "queue";
    private static final int VERIFY_TIMEOUT_MILLIS = 500;

    private SubscriptionManager manager;
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

        manager = new SubscriptionManager(poller, singleton(subscription));
        manager.activateSubscriptions();
    }

    @Test
    public void activatesRegisteredSubscriptions() {
        verify(poller, timeout(VERIFY_TIMEOUT_MILLIS).atLeastOnce()).pollForSingleItemFrom(QUEUE);
        verify(worker, timeout(VERIFY_TIMEOUT_MILLIS).atLeastOnce()).processSingleItem(ITEM);
    }

    @Test
    public void deactivatesRegisteredSubscriptions() {
        manager.deactivateSubscriptions();
        reset(poller, worker);

        verifyNoMoreInteractions(poller, worker);
    }
}