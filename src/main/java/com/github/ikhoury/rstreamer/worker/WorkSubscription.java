package com.github.ikhoury.rstreamer.worker;

import java.util.Collection;

/**
 * This subscription binds workers to a job queue they must process.
 */
public class WorkSubscription {

    private String queue;
    private Collection<Worker> workers;

    public WorkSubscription(String queue, Collection<Worker> workers) {
        this.queue = queue;
        this.workers = workers;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Collection<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(Collection<Worker> workers) {
        this.workers = workers;
    }
}
