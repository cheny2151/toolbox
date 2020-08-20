package cn.cheny.toolbox.scan;

/**
 * 扫描异常
 *
 * @author cheney
 * @date 2020-08-10
 */
public class ScanException extends Exception {
    public ScanException() {
    }

    public ScanException(String message) {
        super(message);
    }

    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
