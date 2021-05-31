package concurrency.counter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class InfiniteCounterThread implements Runnable {
    private String name;
    private Lock lock;
    private volatile AtomicInteger counter;

    public InfiniteCounterThread(String name, Lock lock, AtomicInteger counter) {
        this.name = name;
        this.lock = lock;
        this.counter = counter;
    }

    @Override
    public void run() {
        lock.lock();
        System.out.println("My name is " + name + " and count is " + counter.incrementAndGet());
        lock.unlock();
    }

}
