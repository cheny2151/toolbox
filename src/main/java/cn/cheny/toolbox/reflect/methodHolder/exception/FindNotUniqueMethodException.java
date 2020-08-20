package cn.cheny.toolbox.reflect.methodHolder.exception;

/**
 * 查找到方法不唯一时抛出此异常
 *
 * @author cheney
 * @date 2020-04-01
 */
public class FindNotUniqueMethodException extends MethodHolderReflectException {

    public FindNotUniqueMethodException(String message) {
        super(message);
    }

}
