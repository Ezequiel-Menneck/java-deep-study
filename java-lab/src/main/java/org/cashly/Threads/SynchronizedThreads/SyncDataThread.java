package org.cashly.Threads.SynchronizedThreads;

public class SyncDataThread implements Runnable{
    private final Data data;

    public SyncDataThread(Data data) {
        super();
        this.data = data;
    }

    @Override
    public void run() {
        data.sync();
    }
}
