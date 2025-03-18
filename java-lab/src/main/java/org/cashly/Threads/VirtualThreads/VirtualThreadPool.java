package org.cashly.Threads.VirtualThreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadPool {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                System.out.println("Running task in a virtual thread: "
                        + Thread.currentThread().getName());
            });
        }

        executorService.shutdown();
    }

}
