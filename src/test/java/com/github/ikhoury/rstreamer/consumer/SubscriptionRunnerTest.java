package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.lease.LeaseRunner;
import com.github.ikhoury.rstreamer.worker.WorkSubscription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.github.ikhoury.rstreamer.util.TimeInterval.SHORT_MILLIS;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionRunnerTest {

    @Mock
    private PollingRoutine pollingRoutine;
    @Mock
    private WorkRoutine workRoutine;
    @Mock
    private LeaseRunner leaseRunner;
    @Mock
    private WorkSubscription subscription;

    private SubscriptionRunner runner;

    @Before
    public void setUp() {
        runner = new SubscriptionRunner(subscription, leaseRunner, pollingRoutine, workRoutine);
        runner.start();
    }

    @Test
    public void pollsForItemsInTheBackgroundAfterStart() {
        verify(pollingRoutine, timeout(SHORT_MILLIS).atLeastOnce()).doPoll();
    }

    @Test
    public void processesItemsInTheBackgroundAfterStart() {
        verify(workRoutine, timeout(SHORT_MILLIS).atLeastOnce()).process(anyList());
    }

    @Test
    public void stopsPollingAfterBeingStopped() {
        runner.stop();

        verify(pollingRoutine, after(SHORT_MILLIS).times(0)).doPoll();
    }

    @Test
    public void stopsProcessingAfterBeingStopped() {
        runner.stop();

        verify(workRoutine, after(SHORT_MILLIS).times(0)).process(anyList());
    }
}