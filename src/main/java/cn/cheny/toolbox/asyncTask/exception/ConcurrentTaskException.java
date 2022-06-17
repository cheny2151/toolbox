package cn.cheny.toolbox.asyncTask.exception;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * 并发执行任务异常
 *
 * @author cheney
 * @date 2020-01-14
 */
public class ConcurrentTaskException extends ToolboxRuntimeException {

    public ConcurrentTaskException() {
    }

    public ConcurrentTaskException(String message) {
        super(message);
    }
}
