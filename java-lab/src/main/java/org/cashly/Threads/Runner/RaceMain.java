package org.cashly.Threads.Runner;

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
