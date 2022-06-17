package cn.cheny.toolbox.asyncTask.parallel;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;

/**
 * 并行任务接口
 *
 * @author by chenyi
 * @date 2021/7/26
 */
public interface Parallel<R> extends Closeable {

    R start();

    static <ONE, RESULT> OneParallel<ONE, RESULT> buildOne() {
        return buildOne(null);
    }

    static <ONE, RESULT> OneParallel<ONE, RESULT> buildOne(ExecutorService executor) {
        return new OneParallel<>(executor);
    }

    static <ONE, TWO, THREE, RESULT> ThreeParallel<ONE, TWO, THREE, RESULT> buildThree() {
        return buildThree(null);
    }

    static <ONE, TWO, THREE, RESULT> ThreeParallel<ONE, TWO, THREE, RESULT> buildThree(ExecutorService executor) {
        return new ThreeParallel<>(executor);
    }

    static <ONE, TWO, RESULT> TwoParallel<ONE, TWO, RESULT> buildTwo() {
        return buildTwo(null);
    }

    static <ONE, TWO, RESULT> TwoParallel<ONE, TWO, RESULT> buildTwo(ExecutorService executor) {
        return new TwoParallel<>(executor);
    }

}
