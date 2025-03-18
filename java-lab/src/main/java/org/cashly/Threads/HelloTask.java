package org.cashly.Threads;

public class HelloTask implements Runnable {

    private String taskName;

    public HelloTask(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void run() {
        System.out.println(taskName);
    }
}
