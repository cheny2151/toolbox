package cn.cheny.toolbox.pagingTask.exception;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * 任务中止异常
 *
 * @author by chenyi
 * @Date 2021/5/20
 */
public class TaskInterruptedException extends ToolboxRuntimeException {

    public TaskInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
