package concurrency.counter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CounterDemo {

    private static Lock lock = new ReentrantLock();
    private static AtomicInteger counter = new AtomicInteger(0);
    //а что бы то и нет?! concurrency на практике!
    private static final int maxIterations = 1000000;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newWorkStealingPool();
        System.out.println("Да будет счет!");
        for (int i = 0; i < maxIterations; i++) {
            executor.submit(new InfiniteCounterThread("thread " + (i + 1), lock, counter));
        }
        System.out.println("Пора баеньки!");
        executor.shutdown();
        boolean finished = false;
        do {
            try {
                System.out.println("Ждем окончания потоков");
                if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    finished = true;
                }
            } catch (InterruptedException e) {
                finished = false;
            }
        } while (!finished);
        if (finished) {
            System.out.println("Дождались окончания потоков");
        }
    }

}