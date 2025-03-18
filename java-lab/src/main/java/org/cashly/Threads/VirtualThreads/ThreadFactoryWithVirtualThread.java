package org.cashly.Threads.VirtualThreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadFactoryWithVirtualThread {

    public static void main(String[] args) {
        ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();

        ExecutorService executor =
                Executors.newFixedThreadPool(8, virtualThreadFactory);

        for (int i = 0; i < 8; i++) {
            executor.submit(() -> {
                System.out.println("Running task in a virtual thread: "
                        + Thread.currentThread().getName());
            });
        }

        executor.shutdown();
    }
}


