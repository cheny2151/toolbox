package cn.cheny.toolbox.redis.exception;

/**
 * redis异常
 *
 * @author cheney
 */
public class RedisRuntimeException extends RuntimeException {

    public RedisRuntimeException() {
        super();
    }

    public RedisRuntimeException(String message) {
        super(message);
    }

    public RedisRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisRuntimeException(Throwable cause) {
        super(cause);
    }

    protected RedisRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
