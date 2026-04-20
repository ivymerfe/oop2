package labs.factory.controller;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private final List<Runnable> tasks = new LinkedList<>();

    private volatile boolean isShutdown = false;

    public ThreadPool(int numThreads) {
        for (int i = 0; i < numThreads; i++) {
            Thread.ofVirtual().start(new Worker());
        }
    }

    public void submit(Runnable r) {
        synchronized (tasks) {
            tasks.add(r);
            tasks.notifyAll();
        }
    }

    public void stop() {
        isShutdown = true;
    }

    class Worker implements Runnable {
        @Override
        public void run() {
            while (!isShutdown && !Thread.currentThread().isInterrupted()) {
                Runnable task;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    task = tasks.removeFirst();
                }
                task.run();
            }
        }
    }
}
