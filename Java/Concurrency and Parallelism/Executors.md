## Overview

ExecutorService é uma parte da API do JDK que pode rodar as tarefas de maneira assíncrona. Falando de modo geral _ExercutorService_ automaticamente prove a nós uma pool de Threads e uma API para assigna-las.

## Instanciando um ExercutorService

A maneira mais simples é utilizando um dos métodos de _factory_ da classe de _Executors.class_

Por exemplo, o seguinte código cria uma thread pool com 10 threads.

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
```

Além desse temos uma outra série de métodos para criar um _ExecutorService,_ podemos achar o melhor para nosso use-case na documentação oficial - [Oracle’s official documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html).

### Atribuindo tarefas ao _ExecutorService_

Um _ExecutorService_ pode executar _Runnable e Callable_ tasks.

```java
Runnable runnableTask = () -> {
    try {
        TimeUnit.MILLISECONDS.sleep(300);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
};

Callable<String> callableTask = () -> {
    TimeUnit.MILLISECONDS.sleep(300);
    return "Task's execution";
};

List<Callable<String>> callableTasks = new ArrayList<>();
callableTasks.add(callableTask);
callableTasks.add(callableTask);
callableTasks.add(callableTask);
```

Podemos atribuir tarefas ao _ExercutorService_ usando uma série de métodos, incluindo _execute(),_ qual é herdado a partir da interface de _Executor_