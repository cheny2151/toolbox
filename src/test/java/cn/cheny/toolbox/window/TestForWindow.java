package cn.cheny.toolbox.window;

import cn.cheny.toolbox.reflect.BeanUtils;
import cn.cheny.toolbox.window.output.DefaultBatchResultSplitter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
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

    @Collected(group = "printMulti2")
    public String print2(int i, List<String> test) {
        return test.get(0);
    }

    @Batch(group = "printMulti", threadPoolSize = 3, batchArgIndex = 1)
    public List<String> print(int i, List<String> tests) throws InterruptedException {
        Thread.sleep(100);
        System.out.println(tests.size());
        return tests;
    }

    @Batch(threadPoolSize = 3, batchArgIndex = 1)
    public LinkedList<String> printSelf(int i, List<String> tests) throws InterruptedException {
        Thread.sleep(100);
        System.out.println(tests.size());
        return new LinkedList<>(tests);
    }

    @Batch(threadPoolSize = 3, batchArgIndex = 1, splitter = TestSplitter.class)
    public TestResult printSelfReturnCustomer(int i, List<String> tests) throws InterruptedException {
        Thread.sleep(100);
        System.out.println(tests.size());
        return new TestResult(i, tests);
    }

    @Batch(threadPoolSize = 3, batchArgIndex = 1, splitter = TestArraySplitter.class)
    public TestResultArray printSelfReturnCustomerArray(int i, List<String> tests) throws InterruptedException {
        Thread.sleep(100);
        System.out.println(tests.size());
        return new TestResultArray(i, tests.toArray(new String[1]));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestResult {
        private int i;
        private List<String> rs;
    }

    public static class TestSplitter extends DefaultBatchResultSplitter {

        @Override
        public Object split(Object output, WindowElement element, int index) {
            TestResult testResult = (TestResult) output;
            List<String> list = multiGetList(testResult.getRs(), index, element.size(), List.class);
            TestResult result = new TestResult();
            BeanUtils.copyProperties(result, testResult, "rs");
            result.setRs(list);
            return result;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestResultArray {
        private int i;
        private String[] rs;
    }

    public static class TestArraySplitter extends DefaultBatchResultSplitter {

        @Override
        public Object split(Object output, WindowElement element, int index) {
            TestResultArray testResult = (TestResultArray) output;
            String[] strings = multiGetArray(testResult.getRs(), index, element.size(), String.class);
            TestResultArray result = new TestResultArray();
            BeanUtils.copyProperties(result, testResult, "rs");
            result.setRs(strings);
            return result;
        }
    }


}
