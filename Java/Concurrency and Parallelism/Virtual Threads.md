São criadas pela JVM ao invés do SO (Sistema operacional). JVM cria esses Threads para nós em apenas 1 Thread do SO (dependente do serviço pode ser mais). Como a JVM conhece o que está acontecendo consegue fazer optimizações para essas Threads.

Com elas não temos a limitação de hardware para termos o limite de Threads, conseguimos ter mais eficiência.

### Como utilizar Virtual Threads

```java
public class VirtualThreads {

    public static void main(String[] args) throws InterruptedException {
        Thread virtualThread = Thread.startVirtualThread(() -> {
            System.out.println("Running a virtual Thread " + Thread.currentThread().getName());
        });

        virtualThread.join();
    }

}
```

Nesse exemplo criamos uma Virtual Thread com o método `startVirtualThread` que recebe um Runnable como parâmetro.

### CompletableFuture com Virtual Threads

```java
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
```

Nesse exemplo utilizamos Virtual Threads com **CompletableFuture**. Encadeamos tarefas assíncronas usando os métodos **supplyAsync(), thenApplyAsync() e thenAcceptAsync().** São tarefas que executam em Threads Virtuais

### Virtual Threads Pool

```java
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
```

### Usando ThreadFactory com Virtual Threads

```java
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
```