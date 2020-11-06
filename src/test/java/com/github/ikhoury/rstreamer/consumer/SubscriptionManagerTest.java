package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.config.subsription.SubscriptionManagerConfig;
import com.github.ikhoury.rstreamer.driver.RedisBatchPoller;
import com.github.ikhoury.rstreamer.worker.WorkSubscription;
import com.github.ikhoury.rstreamer.worker.Worker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.github.ikhoury.rstreamer.config.subsription.SubscriptionManageConfigBuilder.defaultSubscriptionManagerConfig;
import static com.github.ikhoury.rstreamer.util.TimeInterval.SHORT_MILLIS;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionManagerTest {

    private static final String ITEM = "item";
    private static final String QUEUE = "queue";

    @Mock
    private Worker worker;
    @Mock
    private RedisBatchPoller poller;

    private SubscriptionManager manager;

    @Before
    public void setUp() {
        SubscriptionManagerConfig config = defaultSubscriptionManagerConfig().build();
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