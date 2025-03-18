package org.cashly.Threads.SynchronizedThreads;

public class Data {
    private boolean outOfSync;
    private String data = "Some Data";

    public synchronized void sync() {
        System.out.println("Synchronizing data...");
        try {
            if (!outOfSync) {
                this.wait();
            }

            outOfSync = false;
            Thread.sleep(5000);
            System.out.println("Synchronized!");
            this.notifyAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void read() {
        System.out.println("Reading data...");
        try {
            if (outOfSync) {
                this.wait();
            }

            Thread.sleep(2000);
            System.out.println("Data: " + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isOutOfSync() {
        return outOfSync;
    }

    public void setOutOfSync(boolean outOfSync) {
        this.outOfSync = outOfSync;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
