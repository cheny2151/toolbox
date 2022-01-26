package cn.cheny.toolbox.window;

import java.util.List;

/**
 * @author by chenyi
 * @date 2022/1/26
 */
public class TestForWindow {

    @Collected(group = "print")
    public String print(String test) {
        return test;
    }

    @Batch(group = "print", threadPoolSize = 3)
    public List<String> print(List<String> tests) throws InterruptedException {
        Thread.sleep(100);
        System.out.println(tests.size());
        return tests;
    }


    @Collected(group = "printMulti")
    public String print(int i, String test) {
        return test;
    }

    @Batch(group = "printMulti", threadPoolSize = 3, batchArgIndex = 1)
    public List<String> print(int i, List<String> tests) throws InterruptedException {
        Thread.sleep(100);
        System.out.println(tests.size());
        return tests;
    }

}
