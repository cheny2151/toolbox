package cn.cheny.toolbox.window;

import cn.cheny.toolbox.window.factory.javassist.JavassistWindowProxyFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author by chenyi
 * @date 2022/1/26
 */
public class WindowMain {

    @Test
    public void test() throws InterruptedException {
        TestForWindow testForWindow = new TestForWindow();
        TestForWindow proxy = new JavassistWindowProxyFactory().createProxy(testForWindow);
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                String str = "test" + finalI;
                String test = proxy.print(finalI, str);
                if (!test.equals(str)) {
                    System.out.println("error");
                }
            });
            thread.start();
        }
        Thread.sleep(10000);
    }

    @Test
    public void printSelf() throws InterruptedException {
        TestForWindow testForWindow = new TestForWindow();
        TestForWindow proxy = new JavassistWindowProxyFactory().createProxy(testForWindow);
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                String str = "test" + finalI;
                List<String> test = null;
                List<String> tests = Arrays.asList(str, str + 2);
                try {
//                    test = proxy.printSelf(finalI, tests);
                    test = proxy.testCallMethod(finalI, tests);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!test.equals(tests)) {
                    System.out.println("error");
                }
            });
            thread.start();
        }
        Thread.sleep(10000);
    }

    @Test
    public void printSelfReturnCustomer() throws InterruptedException {
        TestForWindow testForWindow = new TestForWindow();
        TestForWindow proxy = new JavassistWindowProxyFactory().createProxy(testForWindow);
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                String str = "test" + finalI;
                List<String> tests = Arrays.asList(str, str + 2, str + 3);
                try {
                    TestForWindow.TestResult testResult = proxy.printSelfReturnCustomer(finalI, tests);
                    if (!testResult.getRs().equals(tests)) {
                        System.out.println("error");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        Thread.sleep(10000);
    }

    @Test
    public void printSelfReturnCustomerArray() throws InterruptedException {
        TestForWindow testForWindow = new TestForWindow();
        TestForWindow proxy = new JavassistWindowProxyFactory().createProxy(testForWindow);
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                String str = "test" + finalI;
                List<String> tests = Arrays.asList(str, str + 2, str + 3);
                try {
                    TestForWindow.TestResultArray testResult = proxy.printSelfReturnCustomerArray(finalI, tests);
                    if (!Arrays.asList(testResult.getRs()).equals(tests)) {
                        System.out.println("error");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        Thread.sleep(10000);
    }

    @Test
    public void printSelfReturnCustomerArray2() throws InterruptedException {
        TestForWindow testForWindow = new TestForWindow();
        TestForWindow proxy = new JavassistWindowProxyFactory().createProxy(testForWindow);
        try {
            List<String> tests = Arrays.asList("1", "2", "3");
            proxy.printSelfReturnCustomerArray(1, tests);
            proxy.printSelfReturnCustomerArray(2, tests);
            proxy.printSelfReturnCustomerArray(3, tests);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(10000);
    }
}
