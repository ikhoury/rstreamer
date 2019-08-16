package com.github.ikhoury.consumer;

import com.github.ikhoury.lease.Lease;
import com.github.ikhoury.lease.LeaseBroker;
import com.github.ikhoury.lease.LeaseRunner;
import org.junit.Before;
import org.junit.Test;

import static com.github.ikhoury.util.TimeInterval.SHORT_MILLIS;
import static org.mockito.Mockito.*;

public class PollingThreadTest {

    private static final int NUMBER_OF_RUNS_TO_CHECK = 50;

    private PollingThread pollingThread;
    private PollingRoutine routine;
    private Lease lease;
    private LeaseRunner leaseRunner;
    private LeaseBroker leaseBroker;

    @Before
    public void setUp() {
        leaseBroker = mock(LeaseBroker.class);
        leaseRunner = mock(LeaseRunner.class);
        routine = mock(PollingRoutine.class);
        lease = mock(Lease.class);

        when(leaseBroker.acquireLeaseFor(routine)).thenReturn(lease);

        pollingThread = new PollingThread(leaseBroker, leaseRunner, routine);
    }

    @Test
    public void onlyRunsRoutineWhenStarted() {
        verifyZeroInteractions(leaseRunner);

        pollingThread.start();
        verify(leaseRunner, timeout(SHORT_MILLIS).atLeast(NUMBER_OF_RUNS_TO_CHECK)).run(lease);

        pollingThread.stop();
        reset(leaseRunner);
        verifyZeroInteractions(leaseRunner);
    }
}