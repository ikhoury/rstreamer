package com.github.ikhoury.lease;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class LeaseBrokerTest {

    private static final int MAX_ALLOWED_LEASES = 3;
    private static final int TEST_TIMEOUT_MILLIS = 3000;
    private static final Lease LEASE_TO_RETURN = mock(Lease.class);

    private LeaseBroker broker;

    @Before
    public void setUp() {
        broker = new LeaseBroker(MAX_ALLOWED_LEASES);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void givesOnlyMaximumAllowedNumberOfLeases() {
        int leasesOverLimit = 2;
        int totalRequiredLeases = leasesOverLimit + MAX_ALLOWED_LEASES;
        CompletableFuture[] threadsWithLeases = new CompletableFuture[totalRequiredLeases];

        for (int i = 0; i < totalRequiredLeases; i++) {
            threadsWithLeases[i] = CompletableFuture.runAsync(this::acquireALease);
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

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void reusesLeaseWhenReturned() {
        // Acquire all leases
        for (int i = 0; i < MAX_ALLOWED_LEASES; i++) {
            acquireALease();
        }

        // Attempt to acquire one more lease
        CompletableFuture oneMoreLease = CompletableFuture.runAsync(this::acquireALease);
        assertThat(oneMoreLease.isDone(), equalTo(false));

        // Reuse returned lease
        returnALease();
        oneMoreLease.join();
    }

    private void acquireALease() {
        broker.acquireLeaseFor(this::aTask, "task");
    }

    private void returnALease() {
        broker.returnLease(LEASE_TO_RETURN);
    }

    private void aTask() {
        // do nothing
    }
}