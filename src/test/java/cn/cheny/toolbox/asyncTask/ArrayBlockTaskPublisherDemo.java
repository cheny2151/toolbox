package cn.cheny.toolbox.asyncTask;

import cn.cheny.toolbox.asyncTask.function.Producer;

import java.util.Collections;

/**
 * @author cheney
 * @date 2020-08-26
 */
public class ArrayBlockTaskPublisherDemo {

    public static void main(String[] args) throws InterruptedException {
        demo1();
    }

    public static void demo1() throws InterruptedException {
        AsyncConsumeTaskPublisher taskDealer = new AsyncConsumeTaskPublisher(8, true);
        taskDealer.publisher((Producer<Integer>) publish -> {
            for (int i = 0; i < 100; i++) {
                System.out.println(Thread.currentThread().getName() + ":0");
                publish.push(i);
            }
        }).then(ints -> {
            System.out.println(Thread.currentThread().getName() + ":1");
            return Collections.singletonList(ints.size());
        }).innerThreadName("A").then(ints -> {
            System.out.println(Thread.currentThread().getName() + ":2");
            return Collections.singletonList(ints.size());
        }).innerThreadName("B").subscribe(ints -> {
            System.out.println(Thread.currentThread().getName() + ":3");
        }, 2, "C");
    }


}
