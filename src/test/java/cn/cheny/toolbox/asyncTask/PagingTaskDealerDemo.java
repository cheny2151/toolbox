package cn.cheny.toolbox.asyncTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * @author cheney
 * @date 2020-08-26
 */
public class PagingTaskDealerDemo {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("5");
        strings.add("4");
        strings.add("3");
        strings.add("2");
//        List<TaskResult<Integer>> taskResults = startSlipListTaskWithResult(strings, list -> list.size(), 2);
//        for (TaskResult<Integer> taskResult : taskResults) {
//            System.out.println(taskResult.getResult());
//        }
        PagingTaskDealer.asyncSlipListTask(strings, (limit) -> {
            System.out.println("start");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("end");
        }, 2, () -> {
            System.out.println("callback");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("callback out");
        }, false);
        System.out.println("out");
    }


}
