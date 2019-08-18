package com.github.ikhoury.lease;

import com.github.ikhoury.config.LeaseConfig;
import com.github.ikhoury.consumer.PollingRoutine;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.github.ikhoury.util.TimeInterval.LONG_MILLIS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeaseBrokerTest {

    private static final int MAX_ALLOWED_LEASES = 3;

    private PollingRoutine pollingRoutine;
    private Lease lease;

    private LeaseBroker broker;

    @Before
    public void setUp() {
        pollingRoutine = mock(PollingRoutine.class);
        lease = mock(Lease.class);
        LeaseConfig leaseConfig = mock(LeaseConfig.class);

        when(leaseConfig.getMaxActiveLeases()).thenReturn(MAX_ALLOWED_LEASES);

        broker = new LeaseBroker(leaseConfig);
    }

    @Test(timeout = LONG_MILLIS)
    public void givesOnlyMaximumAllowedNumberOfLeases() {
        int leasesOverLimit = 2;
        int totalRequiredLeases = leasesOverLimit + MAX_ALLOWED_LEASES;
        CompletableFuture[] threadsWithLeases = new CompletableFuture[totalRequiredLeases];

        for (int i = 0; i < totalRequiredLeases; i++) {
            threadsWithLeases[i] = CompletableFuture.runAsync(() -> broker.acquireLeaseFor(pollingRoutine));
        }

        CompletableFuture[] expectedThreadsThatGotALease = Arrays.copyOfRange(threadsWithLeases, 0, MAX_ALLOWED_LEASES);
        CompletableFuture[] expectedThreadsThatAreWaitingForALease = Arrays.copyOfRange(threadsWithLeases, MAX_ALLOWED_LEASES, totalRequiredLeases);

        for (CompletableFuture future : expectedThreadsThatGotALease) {
            future.join();
        }
        for (CompletableFuture future : expectedThreadsThatAreWaitingForALease) {
            assertThat(future.isDone(), equalTo(false));
        }
    }

    @Test(timeout = LONG_MILLIS)
    public void reusesLeaseWhenReturned() {
        // Acquire all leases
        for (int i = 0; i < MAX_ALLOWED_LEASES; i++) {
            broker.acquireLeaseFor(pollingRoutine);
        }

        // Attempt to acquire one more lease
        CompletableFuture oneMoreLease = CompletableFuture.runAsync(() -> broker.acquireLeaseFor(pollingRoutine));
        assertThat(oneMoreLease.isDone(), equalTo(false));

        // Reuse returned lease
        broker.returnLease(lease);
        oneMoreLease.join();
    }

    @Test
    public void returnsCapacityStatistics() {
        assertThat(broker.activeLeaseCount(), equalTo(0));
        assertThat(broker.availableLeaseCount(), equalTo(MAX_ALLOWED_LEASES));

        broker.acquireLeaseFor(pollingRoutine);
        assertThat(broker.activeLeaseCount(), equalTo(1));
        assertThat(broker.availableLeaseCount(), equalTo(MAX_ALLOWED_LEASES - 1));

        broker.returnLease(lease);
        assertThat(broker.activeLeaseCount(), equalTo(0));
        assertThat(broker.availableLeaseCount(), equalTo(MAX_ALLOWED_LEASES));
    }
}