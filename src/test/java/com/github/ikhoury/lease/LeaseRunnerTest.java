package com.github.ikhoury.lease;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static com.github.ikhoury.util.TimeInterval.SHORT_MILLIS;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class LeaseRunnerTest {

    private LeaseRunner leaseRunner;

    private Lease lease;
    private LeaseBroker leaseBroker;
    private Runnable task;
    private ExecutorService executorService;

    @Before
    public void setUp() {
        lease = mock(Lease.class);
        task = mock(Runnable.class);
        leaseBroker = mock(LeaseBroker.class);
        executorService = newSingleThreadExecutor();

        when(lease.getTask()).thenReturn(task);

        leaseRunner = new LeaseRunner(leaseBroker, executorService);
    }

    @Test
    public void runsLeaseTaskAsyncAndReturnsLease() {
        leaseRunner.run(lease);

        verify(task, timeout(SHORT_MILLIS)).run();
        verify(leaseBroker).returnLease(lease);
    }

    @Test
    public void returnsLeaseIfExceptionThrown() {
        when(lease.getTask()).thenThrow(RuntimeException.class);

        leaseRunner.run(lease);

        verify(leaseBroker, timeout(SHORT_MILLIS)).returnLease(lease);
    }

    @Test
    public void shutsDownExecutor() {
        leaseRunner.shutdown();

        assertThat(executorService.isTerminated(), equalTo(true));
    }
}