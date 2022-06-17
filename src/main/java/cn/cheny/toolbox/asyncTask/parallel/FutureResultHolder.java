package cn.cheny.toolbox.asyncTask.parallel;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

import java.util.concurrent.Future;

/**
 * 异步任务结果holder
 *
 * @author by chenyi
 * @date 2021/7/26
 */
public class FutureResultHolder<RESULT> {

    private Future<RESULT> resultFuture;

    public FutureResultHolder(Future<RESULT> resultFuture) {
        this.resultFuture = resultFuture;
    }

    public RESULT get() {
        try {
            return resultFuture.get();
        } catch (Exception e) {
            throw new ToolboxRuntimeException("异步任务结果获取失败", e);
        }
    }
}
