package cn.cheny.toolbox.scan;

import java.net.URL;

/**
 * 是否加载该jar包
 *
 * @author by chenyi
 * @date 2021/5/10
 */
@FunctionalInterface
public interface IsLoadingJar {

    boolean isLoading(JarInfo url);

    class JarInfo {
        public JarInfo(String jarName, URL url) {
            this.jarName = jarName;
            this.url = url;
        }

        private final String jarName;
        private final URL url;

        public String getJarName() {
            return jarName;
        }

        public URL getUrl() {
            return url;
        }
    }

}
