package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.lease.Lease;
import com.github.ikhoury.rstreamer.lease.LeaseBroker;
import com.github.ikhoury.rstreamer.lease.LeaseRunner;
import com.github.ikhoury.rstreamer.worker.BatchWorker;
import com.github.ikhoury.rstreamer.worker.Worker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.github.ikhoury.rstreamer.util.TimeInterval.SHORT_MILLIS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkRoutineTest {

    @Mock
    private LeaseBroker leaseBroker;
    @Mock
    private Worker singleItemWorker;
    @Mock
    private BatchWorker multipleItemsWorker;

    private WorkRoutine routine;

    @Before
    public void setUp() {
        var leaseRunner = mock(LeaseRunner.class);
        var lease = new Lease();

        when(leaseBroker.acquireLease()).thenReturn(lease);
        doAnswer(invocation -> {
            Lease argument = invocation.getArgument(0, Lease.class);
            argument.getTask().run();
            return null;
        }).when(leaseRunner).run(lease);

        routine = new WorkRoutine(asList(singleItemWorker, multipleItemsWorker), leaseBroker, leaseRunner);
    }

    @Test
    public void acquiresALeaseIfAnItemIsReturned() {
        routine.process(singletonList("item"));

        verify(leaseBroker, timeout(SHORT_MILLIS).atLeastOnce()).acquireLease();
    }

    @Test
    public void doesNotAcquireLeaseForEmptyPoll() {
        routine.process(emptyList());

        verify(leaseBroker, after(SHORT_MILLIS).never()).acquireLease();
    }

    @Test
    public void invokesWorkersToProcessSingleItem() {
        var item = "item";

        routine.process(singletonList(item));

        verify(singleItemWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item);
        verify(multipleItemsWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item);
    }

    @Test
    public void invokesWorkersToProcessMultipleItems() {
        var item1 = "1";
        var item2 = "2";
        var items = asList(item1, item2);

        routine.process(items);

        verify(singleItemWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item1);
        verify(singleItemWorker, timeout(SHORT_MILLIS).atLeastOnce()).processSingleItem(item2);
        verify(multipleItemsWorker, timeout(SHORT_MILLIS).atLeastOnce()).processMultipleItems(items);
    }
}