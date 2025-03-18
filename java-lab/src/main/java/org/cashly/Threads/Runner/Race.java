package org.cashly.Threads.Runner;

public class Race {
    private volatile boolean finished;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
