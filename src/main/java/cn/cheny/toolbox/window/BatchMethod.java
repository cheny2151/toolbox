package cn.cheny.toolbox.window;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

import java.lang.reflect.Method;

/**
 * 批处理方法
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public class BatchMethod {

    private final Method method;

    private final Batch batch;

    public BatchMethod(Method method, Batch batch) {
        this.method = method;
        this.batch = batch;
    }

    public Object doBatch(Object target, Object[] args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new ToolboxRuntimeException(e);
        }
    }

    public Method getMethod() {
        return method;
    }

    public Batch getBatch() {
        return batch;
    }
}
