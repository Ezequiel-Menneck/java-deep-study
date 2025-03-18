package org.cashly.Threads.SynchronizedThreads;

public class ReadDataThread implements Runnable {
    private final Data data;

    public ReadDataThread(Data data) {
        super();
        this.data = data;
    }

    @Override
    public void run() {
        data.read();
    }
}
