package cn.cheny.toolbox.reflect.methodHolder.exception;

/**
 * 无指定方法异常
 *
 * @author cheney
 * @date 2019-12-06
 */
public class NoSuchMethodException extends MethodHolderReflectException {

    public NoSuchMethodException(String methodName) {
        super("no such method:'" + methodName + "'");
    }

}
