package com.github.ikhoury.lease;

public class Lease {

    private final String name;
    private final Runnable task;

    Lease(String name, Runnable task) {
        this.task = task;
        this.name = name;
    }

    public Runnable getTask() {
        return task;
    }

    public String getName() {
        return name;
    }
}
