package labs.factory.controller;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private final List<Worker> workers = new LinkedList<>();
    private final List<Runnable> tasks = new LinkedList<>();

    private boolean isShutdown = false;

    public ThreadPool(int numThreads) {
        for (int i = 0; i < numThreads; i++) {
            workers.add(new Worker());
        }
        for (Worker w : workers) {
            w.start();
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

    class Worker extends Thread {
        @Override
        public void run() {
            while (!isShutdown && !isInterrupted()) {
                Runnable task;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {

                            tasks.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    task = tasks.remove(0);
                }
                task.run();
            }
        }
    }
}
