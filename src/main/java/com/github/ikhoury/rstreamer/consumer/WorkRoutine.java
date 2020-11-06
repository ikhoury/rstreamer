package com.github.ikhoury.rstreamer.consumer;

import com.github.ikhoury.rstreamer.lease.LeaseBroker;
import com.github.ikhoury.rstreamer.lease.LeaseRunner;
import com.github.ikhoury.rstreamer.worker.BatchWorker;
import com.github.ikhoury.rstreamer.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WorkRoutine {

    private static final int HOLD_BACK_SECONDS_ON_EMPTY_RESULT = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkRoutine.class);

    private final Collection<Worker> workers;
    private final LeaseBroker leaseBroker;
    private final LeaseRunner leaseRunner;

    public WorkRoutine(Collection<Worker> workers, LeaseBroker leaseBroker, LeaseRunner leaseRunner) {
        this.workers = workers;
        this.leaseBroker = leaseBroker;
        this.leaseRunner = leaseRunner;
    }

    public void process(List<String> items) {
        if (items.isEmpty()) {
            holdBack();
            return;
        }

        var lease = leaseBroker.acquireLease();

        if (items.size() == 1) {
            lease.setTask(() -> processSingleItem(items.get(0)));
        } else {
            lease.setTask(() -> processMultipleItems(items));
        }

        leaseRunner.run(lease);
    }

    private void processSingleItem(String item) {
        workers.forEach(worker -> {
            LOGGER.trace("Worker {} processing single item", worker.getClass().getCanonicalName());
            worker.processSingleItem(item);
        });
    }

    private void processMultipleItems(List<String> items) {
        workers.forEach(worker -> {
            if (worker instanceof BatchWorker) {
                LOGGER.trace("Worker {} processing {} items", worker.getClass().getCanonicalName(), items.size());
                ((BatchWorker) worker).processMultipleItems(items);
            } else {
                LOGGER.trace("Worker {} processing single item", worker.getClass().getCanonicalName());
                items.forEach(worker::processSingleItem);
            }
        });
    }

    private void holdBack() {
        try {
            TimeUnit.SECONDS.sleep(HOLD_BACK_SECONDS_ON_EMPTY_RESULT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
