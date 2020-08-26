package cn.cheny.toolbox.redis.exception;

/**
 * redis脚本执行异常
 *
 * @author cheney
 * @date 2020-08-18
 */
public class RedisScriptException extends RedisRuntimeException {

    public RedisScriptException() {
    }

    public RedisScriptException(String message) {
        super(message);
    }

    public RedisScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisScriptException(Throwable cause) {
        super(cause);
    }

    public RedisScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
