package com.github.ikhoury.consumer;

import com.github.ikhoury.config.RStreamerConfig;
import com.github.ikhoury.driver.RedisBatchPoller;
import com.github.ikhoury.worker.WorkSubscription;
import com.github.ikhoury.worker.Worker;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static com.github.ikhoury.config.RStreamerConfigBuilder.defaultRStreamerConfig;
import static com.github.ikhoury.util.TimeInterval.SHORT_MILLIS;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class SubscriptionManagerTest {

    private static final String ITEM = "item";
    private static final String QUEUE = "queue";

    private SubscriptionManager manager;
    private Worker worker;
    private RedisBatchPoller poller;

    @Before
    public void setUp() {
        worker = mock(Worker.class);
        poller = mock(RedisBatchPoller.class);
        RStreamerConfig config = defaultRStreamerConfig().build();
        WorkSubscription subscription = new WorkSubscription(QUEUE, singleton(worker));

        when(poller.pollForSingleItemFrom(QUEUE)).thenReturn(Optional.of(ITEM));
        when(poller.pollForMultipleItemsFrom(eq(QUEUE), anyInt())).thenReturn(singletonList(ITEM));

        manager = new SubscriptionManager(config, poller);
        manager.addSubscription(subscription);
        manager.activateSubscriptions();
    }

    @Test
    public void activatesRegisteredSubscriptions() {
        verify(poller, timeout(SHORT_MILLIS).atLeastOnce()).pollForSingleItemFrom(QUEUE);
        verify(worker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(ITEM);
    }

    @Test
    public void deactivatesRegisteredSubscriptions() {
        manager.deactivateSubscriptions();
        reset(poller, worker);

        verifyNoMoreInteractions(poller, worker);
    }
}