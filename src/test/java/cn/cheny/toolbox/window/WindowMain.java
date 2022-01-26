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
    public void test2() throws InterruptedException {
        TestForWindow testForWindow = new TestForWindow();
        TestForWindow proxy = new JavassistWindowProxyFactory().createProxy(testForWindow);
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                String str = "test" + finalI;
                List<String> test = null;
                try {
                    test = proxy.printSelf(finalI, Arrays.asList(str));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!test.get(0).equals(str)) {
                    System.out.println("error");
                }
            });
            thread.start();
        }
        Thread.sleep(10000);
    }

}
