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
