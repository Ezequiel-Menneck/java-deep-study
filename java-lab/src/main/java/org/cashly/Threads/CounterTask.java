package org.cashly.Threads;

public class CounterTask implements Runnable {
    private int counter;

    @Override
    public void run() {
        synchronized (this) {
            counter++;
            System.out.println(Thread.currentThread().getName() + ": " + counter);

        }
    }
}
