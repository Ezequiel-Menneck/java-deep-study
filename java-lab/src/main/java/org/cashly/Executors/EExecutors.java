package org.cashly.Executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class EExecutors {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable runnableTask = () -> {
          try {
              TimeUnit.MILLISECONDS.sleep(300);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
        };

        Callable<String> callableTask = () -> {
            TimeUnit.MILLISECONDS.sleep(300);
            return "Task execution";
        };

        List<Callable<String>> callableTasks = new ArrayList<>();
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);

        executor.execute(runnableTask);

        Future<String> future = executor.submit(callableTask);
        String result = executor.invokeAny(callableTasks);
        List<Future<String>> futures = executor.invokeAll(callableTasks);

        System.out.printf("Future Task: %s\n", future.get());
        futures.forEach(e -> {
            try {
                System.out.printf("Futures Tasks: %s\n", e.get());;
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        });
        System.out.printf("Invoke Any: %s\n", result);

        boolean isDone = future.isDone();
        System.out.printf("isDone %b\n", isDone);

        boolean cancel = future.cancel(true);
        boolean isCancelled = future.isCancelled();
        System.out.printf("Cancel %b\n", cancel);
        System.out.printf("Cancelled %b\n", isCancelled);
    }
}
