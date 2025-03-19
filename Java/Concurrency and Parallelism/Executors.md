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

Podemos atribuir tarefas ao _ExercutorService_ usando uma série de métodos, incluindo _execute(),_ qual é herdado a partir da interface de _Executor_ e também _submit()_, _invokeAny()_ e _invokeAll()_.
O método **execute()** é um método void que não nos da a possibilidade de pegar o retorno da execução da tarefa ou conferir o status da tarefa (caso esteja executando)
```java
exercutorService.execute(runnableTask)
```
**submit()** envia uma task do tipo *Callable* ou *Runnable* para um *ExecutorService* e nos retorna um tipo *Future*.
```java
Future<String> future = exercutorService.submit(callableTask);
```
**invokeAny()** atribui uma coleção de tasks para um *ExecutorService*, fazendo com que cada uma seja executada e então retorne o resultado de uma execução bem sucedida de uma tarefa (caso ouve).
```java
String result = executorService.invokeAny(callableTasks);
```
**invokeAll()** atribui uma coleção de tarefas a um *ExercutorService*, executando cada uma e retorna o resultado de todas es execuções de cada task em uma List de objetos do tipo *Future*
```java
List<Future<String>> futures = executorService.invokeAll(callableTasks);
```
## Matando um *ExercutorService*
No geral um *ExecutorService* não é destruído automaticamente quando não há mais nenhuma task para ser processada. Ele continua vivo esperando mais trabalho.

Para isso temos dois métodos onde podemos matar um *ExecutorService*, são eles **shutdown() e shutdownNow()**

O método **shutdown()** não causa a destruição imediata do *ExecutorService*. Ele fará o *ExecutorService* parar de aceitar novas tasks e quando todas tiverem sido executadas e processadas ele se destruíra.
```java
executorService.shutdown()
```
O **shutdownNow()** tenta destruir o *ExecutorService* imediatamente, mas, isso não nos garante que todas as threads que estiverem rodando irão parar ao mesmo tempo.
```java
List<Runnable> notExecutedTaks = executorService.shutdownNow();
```
Esse método nos retorna uma lista de tasks que aguardar ser processadas. Então fica na mão do desenvolvedor oque fazer com elas.

Uma boa maneira de matar um *ExecutorService* (que é a maneira recomendada pela [Oracle](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)) que é utilizar os dois métodos combinados com o **awaitTermination()**.
```java
executorService.shutdown();
try {
	if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		executorService.shutdownNow();
	}
} catch (InterruptedException e) {
	executorService.shutdownNot();
}
```
Com essa abordagem o *ExecutorService* irá primeiro parar de pegar novas tasks para serem executadas e então esperar um período específico para todas as tasks serem completadas. Se o tempo expirar a execução é parada imediatamente.

## Future Interface
Os métodos **submit() e invokeAll()** nos retornam um objeto ou uma coleção do tipo Future, o qual nos permite verificar o resultado da execução da task ou checar o status da task (se estiver em execução).

A interface Future nos prove um método especial de bloquear, **get()**, o qual retorna o estado atual da *Callable* task em execução ou null se for uma *Runnable task*:
```java
Future<String> future = executorService.submit(callableTask);
String result = null;
try {
	result = future.get();
} catch (InterruptedException | ExecutionException e) {
	e.printStackTrace();
}
```
Utilizando o método *get()* enquanto a task continua em processando fara com que a execução seja bloqueada até que a tarefa seja executada corretamente e o resultado esteja disponível.
Com tempos de bloqueio muito altos causados pelo método *get()* a performance da aplicação pode cair. Se o resultado desse dado não é crucial, podemos evitar esse problema usando timeouts:
```java
String result = future.get(200, TimeUnit.MILLISECONDS);
```
Se o tempo de execução for maior que o especificado (200ms nesse caso), uma exceção de TimeoutException será lançada.

Podemos utilizar o método *isDone()* para verificar se a task atribuída já foi processada ou não.

A interface Future também prove um método para cancelarmos a execução de uma task com *cancel()* e verificar se ja foi cancelada com o método *isCancelled()*.
```java
boolean canceled = future.cancel(true);
boolean isCanceled = future.isCancelled();
```

