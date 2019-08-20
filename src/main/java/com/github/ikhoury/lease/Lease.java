package com.github.ikhoury.lease;

public class Lease {

    private final String name;
    private Runnable task;

    public Lease(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }
}
