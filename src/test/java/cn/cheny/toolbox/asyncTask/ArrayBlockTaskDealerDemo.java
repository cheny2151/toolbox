package cn.cheny.toolbox.asyncTask;

import cn.cheny.toolbox.asyncTask.function.Producer;
import cn.cheny.toolbox.asyncTask.pool2.AsyncConsumeTaskDealerPool;
import cn.cheny.toolbox.asyncTask.pool2.AsyncConsumeTaskDealerPooled;
import cn.cheny.toolbox.other.order.Orders;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author cheney
 * @date 2020-08-26
 */
public class ArrayBlockTaskDealerDemo {

    public static void main(String[] args) throws InterruptedException {
        interrupted();
    }

    public static void demo1() throws InterruptedException {
        long l = System.currentTimeMillis();
        AsyncConsumeTaskDealer taskDealer = new AsyncConsumeTaskDealer(8, true);
        AsyncConsumeTaskDealer.FutureResult<HashMap<String, Object>> futureResult = taskDealer.threadName("test").continueWhenSliceTaskError(false)
                .submit(() -> 2000, limit -> {
                    System.out.println(Thread.currentThread().getName() + ":put data");
                    int num = limit.getNum();
                    ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<>();
                    for (int i = 0; i < limit.getSize(); i++) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("num", num + i);
                        hashMaps.add(hashMap);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return hashMaps;
                }, data -> {
                    int i = 1 / 0;
                    try {
                        System.out.println(Thread.currentThread().getName() + ":" + data.size());
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return data;
                }, 100);
        List<HashMap<String, Object>> results = futureResult.getResults();
        System.out.println(results.size());
        System.out.println(System.currentTimeMillis() - l);
        results.forEach(System.out::println);
    }

    public static void demo2() throws InterruptedException {
        long l = System.currentTimeMillis();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AsyncConsumeTaskDealer asyncConsumeTaskDealer = new AsyncConsumeTaskDealer(8, true);
        AsyncConsumeTaskDealer.FutureResult<TestEntity> find_end = asyncConsumeTaskDealer.submitOrderByExtremum(
                () -> 2000,
                limit -> {
                    System.out.println("max:" + limit.getExtremum());
                    ArrayList<TestEntity> integers = new ArrayList<>();
                    for (int i = 0; i < limit.getSize(); i++) {
                        TestEntity testEntity = new TestEntity();
                        testEntity.setId(atomicInteger.getAndIncrement());
                        integers.add(testEntity);
                    }
                    System.out.println("find end");
                    return integers;
                },
                data -> {
                    try {
                        System.out.println(Thread.currentThread().getName());
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("size:" + data.size());
                    return data.stream().filter(e -> e.getId() > 10).collect(Collectors.toList());
                },
                100);
        find_end.getResults();
        System.out.println("~~~");
        System.out.println(System.currentTimeMillis() - l);
    }

    public static void demo3() throws InterruptedException {
        long l = System.currentTimeMillis();
        ArrayList<String> objects = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            objects.add("test" + i);
        }
        AsyncConsumeTaskDealer asyncConsumeTaskDealer = new AsyncConsumeTaskDealer(8, false);
        AsyncConsumeTaskDealer.FutureResult<String> futureResult = asyncConsumeTaskDealer.map(objects, 10, data -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Collections.singletonList("fin");
        });
        System.out.println("test");
        List<String> results = futureResult.getResults();
        System.out.println(results);
        System.out.println(System.currentTimeMillis() - l);
    }

    public static void demo4() {
        AsyncConsumeTaskDealer asyncConsumeTaskDealer = new AsyncConsumeTaskDealer(8, false);
        AsyncConsumeTaskDealer.FutureResult<Integer> futureResult = asyncConsumeTaskDealer.continueWhenSliceTaskError(true)
                .submit((Producer<String>) publish -> {
                    for (int i = 0; i < 2000; i++) {
                        publish.push("test" + i);
                    }
                }, text -> {
                    System.out.println(text.get(0));
                    return Collections.singletonList(text.size());
                });
        System.out.println(futureResult.getResults());
    }

    public static void orderType() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 2001; i++) {
            list.add("test" + i);
        }
        AsyncConsumeTaskDealer asyncConsumeTaskDealer = new AsyncConsumeTaskDealer(8, true);
        AsyncConsumeTaskDealer.FutureResult<String> futureResult = asyncConsumeTaskDealer.continueWhenSliceTaskError(true)
                .orderType(Orders.OrderType.desc)
                .map(list, 1, e -> e);
        List<String> results = futureResult.getResults();
        results.forEach(System.out::println);
        System.out.println(results.size());
    }

    public static void interrupted() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 2001; i++) {
            list.add("test" + i);
        }
        AsyncConsumeTaskDealer asyncConsumeTaskDealer = new AsyncConsumeTaskDealer(8, false);
        AsyncConsumeTaskDealer.FutureResult<String> futureResult = asyncConsumeTaskDealer.continueWhenSliceTaskError(false)
                .orderType(Orders.OrderType.asc)
                .map(list, 100, e -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    if (e.size() == 1) {
                        throw new IllegalArgumentException("test");
                    }
                    return e;
                });
        List<String> results = futureResult.getResults();
        results.forEach(System.out::println);
        System.out.println(results.size());
    }

    @Test
    public void testPool() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 2001; i++) {
            list.add("test" + i);
        }
        AsyncConsumeTaskDealerPool pool = AsyncConsumeTaskDealerPool.builder().threadNum(8).build();
        for (int i = 0; i < 9; i++) {
            if (i > 0){
                Thread.sleep(5000);
            }
            try (AsyncConsumeTaskDealerPooled pooled = pool.borrowObject()){
                AsyncConsumeTaskDealer.FutureResult<String> futureResult = pooled.continueWhenSliceTaskError(false)
                        .orderType(Orders.OrderType.asc)
                        .map(list, 100, e -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                            return e;
                        });
                List<String> results = futureResult.getResults();
                System.out.println(i + ":" + results.size());
            }
        }
    }


    private static class TestEntity implements ExtremumField<Integer> {

        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public Integer getExtremumValue() {
            return id;
        }
    }

}
