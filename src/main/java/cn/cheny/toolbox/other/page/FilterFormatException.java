package cn.cheny.toolbox.other.page;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * 过滤信息错误类
 * Created by cheny on 2017/9/24.
 */
public class FilterFormatException extends ToolboxRuntimeException {

    public FilterFormatException() {
        super();
    }

    public FilterFormatException(String message) {
        super(message);
    }

    public FilterFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterFormatException(Throwable cause) {
        super(cause);
    }

    protected FilterFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
