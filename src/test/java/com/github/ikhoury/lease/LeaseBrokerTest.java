package com.github.ikhoury.lease;

import com.github.ikhoury.config.subsription.LeaseConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.github.ikhoury.util.TimeInterval.LONG_MILLIS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeaseBrokerTest {

    private static final int MAX_ALLOWED_LEASES = 3;

    private Lease lease;
    private LeaseBroker broker;

    @Before
    public void setUp() {
        lease = mock(Lease.class);
        LeaseConfig leaseConfig = mock(LeaseConfig.class);

        when(leaseConfig.getMaxActiveLeases()).thenReturn(MAX_ALLOWED_LEASES);

        broker = new LeaseBroker(leaseConfig, "queue");
    }

    @Test(timeout = LONG_MILLIS)
    public void givesOnlyMaximumAllowedNumberOfLeases() {
        int leasesOverLimit = 2;
        CompletableFuture[] extraLeases = new CompletableFuture[leasesOverLimit];

        for (int i = 0; i < MAX_ALLOWED_LEASES; i++) {
            broker.acquireLease();
        }

        for (int i = 0; i < leasesOverLimit; i++) {
            extraLeases[i] = CompletableFuture.runAsync(() -> broker.acquireLease());
        }

        for (CompletableFuture future : extraLeases) {
            assertThat(future.isDone(), equalTo(false));
        }
    }

    @Test(timeout = LONG_MILLIS)
    public void reusesLeaseWhenReturned() {
        // Acquire all leases
        for (int i = 0; i < MAX_ALLOWED_LEASES; i++) {
            broker.acquireLease();
        }

        // Attempt to acquire one more lease
        CompletableFuture oneMoreLease = CompletableFuture.runAsync(() -> broker.acquireLease());
        assertThat(oneMoreLease.isDone(), equalTo(false));

        // Reuse returned lease
        broker.returnLease(lease);
        oneMoreLease.join();
    }

    @Test
    public void returnsCapacityStatistics() {
        assertThat(broker.activeLeaseCount(), equalTo(0));
        assertThat(broker.availableLeaseCount(), equalTo(MAX_ALLOWED_LEASES));

        broker.acquireLease();
        assertThat(broker.activeLeaseCount(), equalTo(1));
        assertThat(broker.availableLeaseCount(), equalTo(MAX_ALLOWED_LEASES - 1));

        broker.returnLease(lease);
        assertThat(broker.activeLeaseCount(), equalTo(0));
        assertThat(broker.availableLeaseCount(), equalTo(MAX_ALLOWED_LEASES));
    }
}