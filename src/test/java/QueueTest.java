import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {

    private static final Queue<Integer> queue = new LinkedList<>(); // Non-Thread-Safe

    @Test
    void Non_Thread_Safe_Queue_Concurrency_Test_With_Three_3() throws InterruptedException {
        Runnable task = () -> { // 각 Thread 작업 정의
            for(int i = 1; i <= 100; i++) {
                queue.offer(i);
            }
        };

        Thread t1 = new Thread(task); // Thread1
        Thread t2 = new Thread(task); // Thread2
        Thread t3 = new Thread(task); // Thread3

        t1.start();
        t2.start();
        t3.start();

        t1.join(); // 메인 스레드 t1 스레드 끝날때까지 대기
        t2.join(); // 메인 스레드 t2 스레드 끝날때까지 대기
        t3.join(); // 메인 스레드 t3 스레드 끝날때까지 대기

        int expected = 300;
        int result = queue.size();

        System.out.println("큐 크기 = " + result);
        assertNotEquals(expected, result);
    }

    @Test
    void Non_Thread_Safe_Queue_Concurrency_Test_With_Thread_100() throws InterruptedException {

        int taskCount = 100;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(taskCount);

        Runnable task = () -> {

            try {
                startLatch.await();

                for(int i = 1; i <= 100; i++) {
                    queue.offer(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }

            for(int i = 1; i <= 100; i++) {
                queue.offer(i);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(100);

        for(int i = 0; i < taskCount; i++) {
            executor.submit(task);
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(1, TimeUnit.MINUTES);
        if(!completed) {
            throw new IllegalStateException("작업이 1분 내에 끝나지 않았습니다.");
        }

        executor.shutdown();

        System.out.println("queue size = " + queue.size());
        assertNotEquals(10000, queue.size());
    }
}
