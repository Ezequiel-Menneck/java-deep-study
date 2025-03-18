package org.cashly.Threads.VirtualThreads;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureVirtualThreads {

    public static void main(String[] args) {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> "Virtual Thread")
                .thenApplyAsync(String::toUpperCase)
                .thenAcceptAsync(upperCaseResult -> {
                    System.out.printf("Result %s in thread %s", upperCaseResult, Thread.currentThread().getName());
                });

        future.join();
    }

}
