# 1 - Oque é uma thread?

A definição academia é: “uma forma de um processo dividir a si mesmo em duas ou mais tarefas que podem ser executadas concorrentemente”. No java conseguimos fazer isso utilizando a Classe `Thread`. Dentro de uma Thread podemos especificar tarefas, que são definidas pelo tipo `Runnable` um tipo executável, onde temos a implementação da tarefa.

# 2 - Multi-thread em Java

Por padrão até o JDK 20, uma thread quando aberta no código, abrirá uma thread na plataforma (SO). Essa thread será incluída em todo o ciclo do Escalonador de Processos.

## 2.1 - Quando isso é bom?

Quando há algum bloco de código síncrono (synchronized) ou quando está sendo feito alguma operação de IO ou quando é chamado algum código nativo em C ou C++ (sim, é possível).

## 2.2 - Custo de uma thread no Java

Por padrão em Java uma thread é apenas um wrapper de uma thread na plataforma, porém, isso tem um custo. Para cada thread de plataforma que abrimos no Java, aumentamos em torno de 2MB de consumo de memória. É um aumento de memória extremamente desproporcional ao ganho de performance para a maioria dos processamentos.

# 3 - Virtual Threads

São pseudo-threads diretamente gerenciadas pela JVM e rodando em threads previamente alocadas por ela.

Quando uma aplicação Java é iniciada, é configurado um pool de threads (plataforma) e elas ficarão disponíveis para que a aplicação possa alocar ali as threads virtuais. São threads gerenciadas pelo Java que rodam em threads de plataforma no SO.

## 3.1 - Qual o ganho com isso?

Um bom exemplo é um exemplo de busca de usuários em um banco de dados, onde a chamada ao banco bloqueia a execução da thread. Quando a JVM percebe que, dentro de uma virtual thread, há algum bloqueio, automaticamente ela move o contexto para o heap memory e inicializa a próxima virtual thread disponível. Isso minima aquele efeito de ter uma thread “segurando” o núcleo de processamento sem utilizá-lo. Quando o bloqueio for resolvido, a JVM move novamente o contexto do heap memory para a thread e retorna a execução.

### 3.1.1 - Quando devo usar?

Quando houver algum código que ficará bloqueado esperando o retorno de algum recurso/chamada que seja síncrono. Exemplo: Chamada a API external, consumo de banco de dados

### 3.1.2 - Quando não devo usar?

Nos demais casos, onde há computando de dados que já estejam me memória ou parallelStream(). Inclusive nesses casos teremos uma PERDA de desempenho de migrarmos para uma Virtual Thread.

# 4 - Threads Simultâneas

Como threads, conseguem operar simultaneamente? E se uma thread afetar outra?

Se tivermos uma tarefa de contagem com a seguinte implementação

```java
public class CounterTask implements Runnable {
	private int counter;
	
	@Override
	public void run() {
		count++;
		System.out.println(Thread.currentThread().getName() + ": " + counter);
	}
}

public class Main {
    public static void main(String[] args) {
        CounterTask task = new CounterTask();
        Thread counter1 = new Thread(task);
        Thread counter2 = new Thread(task);
        Thread counter3 = new Thread(task);
        Thread counter4 = new Thread(task);

        counter1.start();
        counter2.start();
        counter3.start();
        counter4.start();
    }
}
```

Quando executarmos isso veremos que a contagem não será sequencial, visto que as threads não executam sequencialmente, porém, temos um modo onde conseguimos garantir isso, que é quando utilizamos o bloco de synchronized:

```java
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
```

A _keywork_ garante que tudo que esteja dentro desse bloco seja executado apenas se nenhuma thread esteja executando, como um semáforo, onde um executa de cada vez. Nesse caso utilizamos um conceito de _Mutex_, mutex é uma chave que fecha ao bloco delimitado. Nesse exemplo nossa chave é o objeto (this), pois é ele que tem o contador que utilizamos pela threads. Ou seja, o mutex sincroniza isso para nós garantindo que executaremos em ordem

## 4.1 - E se a Thread depender de um recurso?

Nesse caso, como faremos quando uma thread tem uma dependencia de outro recurso para poder ser executada?

```java
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
```

```java
package org.cashly.Threads.SynchronizedThreads;

public class ReadDataThread implements Runnable {
    private final Data data;

    public ReadDataThread(Data data) {
        super();
        this.data = data;
    }

    @Override
    public void run() {
        data.read();
    }
}

```

```java
package org.cashly.Threads.SynchronizedThreads;

public class SyncDataThread implements Runnable{
    private final Data data;

    public SyncDataThread(Data data) {
        super();
        this.data = data;
    }

    @Override
    public void run() {
        data.sync();
    }
}

```

```java
package org.cashly.Threads.SynchronizedThreads;

public class OnlineOfflineSystem {

    public static void main(String[] args) {
        Data data = new Data();
        data.setOutOfSync(true);

        Thread readData = new Thread(new ReadDataThread(data));
        Thread syncData = new Thread(new SyncDataThread(data));
        readData.start();

        syncData.setDaemon(true);
        syncData.start();
    }

}

```

Nesse exemplo conseguimos utilizar um campo auxiliar para fazer o sync das Threads, utilizamos o outOfSync para dizermos que a Thread de leitura necessita que a Thread de sync seja executada primeiro para assim poder lermos o dado, essa é uma prática comum quando necessitados trabalhar com Threads simultaneas que uma depende da outra.

Caso não passasemos o outOfSync como true temos um modo de escapar de um this.wait() infinito, já que o resultado de false por default viraria true e cairia ali esperando um notify() de alguém. Setando a Thread como Daemon(), dizemos que é um Thread que sera morta quando apenas ela estiver em execução, ou seja, no nosso programas temos apenas duas, assim que a readData finalizar a aplicação por padrão mata a Thread de sync, que estaria travada.

## 4.2 - E se eu precisar encerar uma Thread?

Em Java, por default não temos algo que nos auxilie nisso, não temos esse método built-in.

```java
public class Race {
    private volatile boolean finished;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
```

```java
public class Runner implements Runnable {
    public Runner(Race race, String runner) {
        super();
        this.race = race;
        this.runner = runner;
    }

    public Race race;
    public String runner;

//    @Override
//    public void run() {
//        System.out.println(runner + " running...");
//        System.out.println("Winner: " + runner);
//    }

    @Override
    public void run() {
        System.out.println(runner + " running...");
        if (!race.isFinished()) {
            race.setFinished(true);
            System.out.println("Winner: " + runner);
            return;
        }
        System.out.println(runner + " lost :(");
    }
}
```

```java
public class RaceMain {
    public static void main(String []args) {
        Race race = new Race();
        Thread runner1 = new Thread(new Runner(race, "Runner 1"));
        Thread runner2 = new Thread(new Runner(race, "Runner 2"));
        System.out.println("Starting...");
        runner1.start();
        runner2.start();
    }
}
```

Nesse caso para podermos fazer o “cancelamento/finalização” da Thread podemos utilizar um atributo auxiliar, nesse caso o finished. No momento em que esse atributo auxiliar é passado para true a função de Run true retorna e o outro “corredor” não consegue ser mais o ganhador, devido ao atributo estar como true e ele precisar ser falso para ser o ganhador. Para garantir que isso funcione utilizamos a keyword `volatile` oque garante que ambas as Threas irão ler o valor de `finished` de maneira consistente, algo atomico assim digamos.

**Ref:**

[](https://medium.com/@boschtechbr/java-virtual-thread-conceito-e-quando-ou-n%C3%A3o-usar-7c56238e951d)[https://medium.com/@boschtechbr/java-virtual-thread-conceito-e-quando-ou-não-usar-7c56238e951d](https://medium.com/@boschtechbr/java-virtual-thread-conceito-e-quando-ou-n%C3%A3o-usar-7c56238e951d)

[https://www.youtube.com/watch?v=Z8ykx7ze_Co](https://www.youtube.com/watch?v=Z8ykx7ze_Co)

[https://medium.com/swlh/understanding-java-threads-once-and-for-all-711f71e0ec1e](https://medium.com/swlh/understanding-java-threads-once-and-for-all-711f71e0ec1e)