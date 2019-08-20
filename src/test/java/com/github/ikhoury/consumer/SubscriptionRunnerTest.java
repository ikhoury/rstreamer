package com.github.ikhoury.consumer;

import com.github.ikhoury.lease.Lease;
import com.github.ikhoury.lease.LeaseBroker;
import com.github.ikhoury.lease.LeaseRunner;
import com.github.ikhoury.worker.BatchWorker;
import com.github.ikhoury.worker.WorkSubscription;
import com.github.ikhoury.worker.Worker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.ikhoury.util.TimeInterval.SHORT_MILLIS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class SubscriptionRunnerTest {

    private static final String WORK_QUEUE = "workQueue";

    private SubscriptionRunner subscriptionRunner;
    private PollingRoutine routine;
    private LeaseRunner leaseRunner;
    private LeaseBroker leaseBroker;
    private Worker singleItemWorker;
    private BatchWorker multipleItemsWorker;

    @Before
    public void setUp() {
        leaseBroker = mock(LeaseBroker.class);
        routine = mock(PollingRoutine.class);
        singleItemWorker = mock(Worker.class);
        multipleItemsWorker = mock(BatchWorker.class);
        leaseBroker = mock(LeaseBroker.class);
        leaseRunner = mock(LeaseRunner.class);
        WorkSubscription subscription = mock(WorkSubscription.class);
        Lease lease = new Lease(WORK_QUEUE);

        when(subscription.getQueue()).thenReturn(WORK_QUEUE);
        when(subscription.getWorkers()).thenReturn(asList(singleItemWorker, multipleItemsWorker));
        when(leaseBroker.acquireLeaseFor(any())).thenReturn(lease);
        doAnswer(invocation -> {
            Lease argument = invocation.getArgument(0, Lease.class);
            argument.getTask().run();
            return null;
        }).when(leaseRunner).run(lease);

        subscriptionRunner = new SubscriptionRunner(subscription, leaseBroker, leaseRunner, routine);
    }

    @Test
    public void acquiresALeaseIfAnItemIsReturned() {
        when(routine.doPoll()).thenReturn(singletonList("item"));

        subscriptionRunner.start();

        verify(leaseBroker, timeout(SHORT_MILLIS).atLeastOnce()).acquireLeaseFor(WORK_QUEUE);
    }

    @Test
    public void doesNotAcquireLeaseForEmptyPoll() {
        when(routine.doPoll()).thenReturn(emptyList());

        subscriptionRunner.start();

        verify(leaseBroker, after(SHORT_MILLIS).never()).acquireLeaseFor(WORK_QUEUE);
    }

    @Test
    public void invokesWorkersToProcessSingleItem() {
        String item = "item";
        when(routine.doPoll()).thenReturn(singletonList(item));

        subscriptionRunner.start();

        verify(singleItemWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item);
        verify(multipleItemsWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item);
    }

    @Test
    public void invokesWorkersToProcessMultipleItems() {
        String item1 = "1";
        String item2 = "2";
        List<String> items = asList(item1, item2);
        when(routine.doPoll()).thenReturn(items);

        subscriptionRunner.start();

        verify(singleItemWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item1);
        verify(singleItemWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item2);
        verify(multipleItemsWorker, timeout(SHORT_MILLIS).atLeastOnce()).processMultipleItems(items);
    }

    @After
    public void tearDown() {
        subscriptionRunner.stop();
    }
}