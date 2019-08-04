package com.github.ikhoury.dispatcher;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class PollingThreadTest {

    private static final int WAIT_TIME_IN_MILLIS = 500;

    private PollingRoutine routine;
    private PollingThread pollingThread;

    @Before
    public void setUp() {
        routine = mock(PollingRoutine.class);
        pollingThread = new PollingThread(routine);
    }

    @Test
    public void verifyThreadLifecycle() throws InterruptedException {
        pollingThread.start();
        giveItSomeTime();

        pollingThread.interrupt();
        giveItSomeTime();

        if (pollingThread.isAlive()) {
            fail("Thread did not stop!");
        }
        verify(routine, atLeastOnce()).doPoll();
    }

    private void giveItSomeTime() throws InterruptedException {
        pollingThread.join(WAIT_TIME_IN_MILLIS);
    }
}