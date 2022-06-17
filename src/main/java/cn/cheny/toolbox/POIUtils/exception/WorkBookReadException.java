package cn.cheny.toolbox.POIUtils.exception;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

public class WorkBookReadException extends ToolboxRuntimeException {

    public WorkBookReadException(String message) {
        super(message);
    }

    public WorkBookReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
