package cn.cheny.toolbox.scan;

/**
 * 是否加载该jar包
 *
 * @date 2021/5/10
 * @author by chenyi
 */
@FunctionalInterface
public interface IsLoadingJar {

    boolean isLoading(PathScanner.JarUrl url);

}
