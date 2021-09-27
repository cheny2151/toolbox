package cn.cheny.toolbox.scan;

import java.net.URL;

/**
 * 是否加载该jar包
 *
 * @date 2021/5/10
 * @author by chenyi
 */
@FunctionalInterface
public interface IsLoadingJar {

    boolean isLoading(URL url);

}
