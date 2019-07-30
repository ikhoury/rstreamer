package com.github.ikhoury.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each {@code WorkSubscription} will have a {@code PollingThread} to service it.
 * The subscription is packaged in a {@code PollingRoutine} which is executed by this thread
 * forever until it is interrupted.
 */
class PollingThread extends Thread {

    PollingThread(PollingRoutine routine) {
        super(new PollingRoutineRunner(routine));
        setName("PollingThread-" + getId());
    }

    private static class PollingRoutineRunner implements Runnable {

        private static final Logger LOGGER = LoggerFactory.getLogger(PollingThread.class);

        private final PollingRoutine routine;

        PollingRoutineRunner(PollingRoutine routine) {
            this.routine = routine;
        }

        @Override
        public void run() {
            String queue = routine.getWorkQueue();
            LOGGER.debug("Running subscription for {}", queue);

            while (!Thread.interrupted()) {
                routine.doPoll();
            }

            LOGGER.debug("Closed subscription for {}", queue);
        }
    }
}
