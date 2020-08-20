package cn.cheny.toolbox.pagingTask;

import cn.cheny.toolbox.other.page.Limit;
import lombok.Data;

/**
 * 分页任务执行结果
 *
 * @author cheney
 * @date 2019-09-12
 */
@Data
public class TaskResult<T> {

    public static <T> TaskResult<T> success(T result, Limit limit) {
        TaskResult<T> taskResult = new TaskResult<>();
        taskResult.setSuccess(true);
        taskResult.setResult(result);
        taskResult.setLimitNum(limit.getNum());
        taskResult.setLimitSize(limit.getSize());
        return taskResult;
    }

    public static <T> TaskResult<T> fail(Throwable error, Limit limit) {
        TaskResult<T> taskResult = new TaskResult<>();
        taskResult.setSuccess(false);
        taskResult.setError(error);
        taskResult.setLimitNum(limit.getNum());
        taskResult.setLimitSize(limit.getSize());
        return taskResult;
    }

    private T result;

    private int limitNum;

    private int limitSize;

    private Throwable error;

    private boolean success;
}
