package cn.cheny.toolbox.pagingTask;

import java.util.ArrayList;
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
        demo1();
    }

    public static void demo1() throws InterruptedException {
        long l = System.currentTimeMillis();
        ArrayBlockTaskDealer taskDealer = new ArrayBlockTaskDealer(8, true);
        taskDealer.setThreadName("test");
        ArrayBlockTaskDealer.FutureResult<HashMap<String, Object>> futureResult = taskDealer.execute(() -> 2000, limit -> {
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
        ArrayBlockTaskDealer arrayBlockTaskDealer = new ArrayBlockTaskDealer(8, false);
        ArrayBlockTaskDealer.FutureResult<TestEntity> find_end = arrayBlockTaskDealer.executeOrderByExtremum(
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
