package com.github.ikhoury.lease;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

import static com.github.ikhoury.util.TimeInterval.SHORT_MILLIS;
import static org.mockito.Mockito.*;

public class LeaseRunnerTest {

    private LeaseRunner leaseRunner;

    private Lease lease;
    private Runnable task;

    @Before
    public void setUp() {
        lease = mock(Lease.class);
        task = mock(Runnable.class);

        when(lease.getTask()).thenReturn(task);

        leaseRunner = new LeaseRunner(Executors.newSingleThreadExecutor());
    }

    @Test
    public void runsLeaseTaskAsync() {
        leaseRunner.run(lease);

        verify(task, timeout(SHORT_MILLIS)).run();
    }
}