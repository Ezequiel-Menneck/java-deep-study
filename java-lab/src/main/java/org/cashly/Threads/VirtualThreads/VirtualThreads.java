package org.cashly.Threads.VirtualThreads;

public class VirtualThreads {

    public static void main(String[] args) throws InterruptedException {
        Thread virtualThread = Thread.startVirtualThread(() -> {
            System.out.println("Running a virtual Thread " + Thread.currentThread().getName());
        });

        virtualThread.join();
    }

}
