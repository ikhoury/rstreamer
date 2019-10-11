package com.github.ikhoury.rstreamer.lease;

public class Lease {

    private Runnable task;

    public Lease() {
        this.task = () -> {
            // No Op
        };
    }

    public Runnable getTask() {
        return task;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }
}
