package cn.cheny.toolbox.asyncTask;

import cn.cheny.toolbox.asyncTask.poolmanager.ExpiredResourceManager;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author by chenyi
 * @date 2021/8/18
 */
public class ResourceManagerTest {

    @Test
    public void test() throws InterruptedException {
        ExpiredResourceManager<Integer> manager = new ExpiredResourceManager<>(20000, 20000, TimeUnit.MILLISECONDS);
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        for (int i = 0; i < 200; i++) {
            int finalI = i;
            executorService.execute(() -> {
                for (int j = 0; j < 100; j++) {
//                    try {
//                        Thread.sleep(3);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    boolean put = manager.put(finalI * 500 + j);
                    if (!put) {
                        fail.incrementAndGet();
                    } else {
                        success.incrementAndGet();
                    }
                }
            });
        }
        AtomicInteger integer = new AtomicInteger(0);
        AtomicInteger integer2 = new AtomicInteger(0);
        for (int i = 0; i < 200; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < 100000; j++) {
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    Integer poll = manager.poll();
                    if (poll != null) {
                        integer2.incrementAndGet();
                    }
                }
                int i1 = integer.incrementAndGet();
                if (i1 == 200) {
                    System.out.println("finish get");
                    System.out.println(integer2.get());
                    System.out.println(success.get());
                    System.out.println(fail.get());
                }
            });
        }
        Thread.sleep(200000);
    }

}
