package cn.cheny.toolbox.scan;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import static cn.cheny.toolbox.scan.PathScanner.PACKAGE_SEPARATE_CHARACTER;
import static cn.cheny.toolbox.scan.PathScanner.URL_SEPARATE_CHARACTER;

/**
 * package相关工具类
 *
 * @author by chenyi
 * @date 2021/12/27
 */
@Slf4j
public class PackageUtils {

    /**
     * jar文件的magic头
     */
    private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

    public static String replaceUrlToPackage(String url) {
        return url.replaceAll(URL_SEPARATE_CHARACTER, PACKAGE_SEPARATE_CHARACTER);
    }

    public static String replacePackageToUrl(String pack) {
        return pack.replaceAll("\\.", URL_SEPARATE_CHARACTER);
    }

    /**
     * 判断url所对应的资源是否为jar包
     */
    public static boolean isJar(URL url) {
        return isJar(url, new byte[JAR_MAGIC.length]);
    }

    private static boolean isJar(URL url, byte[] buffer) {
        InputStream is = null;
        try {
            is = url.openStream();
            is.read(buffer, 0, JAR_MAGIC.length);
            if (Arrays.equals(buffer, JAR_MAGIC)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found JAR: " + url);
                }
                return true;
            }
        } catch (Exception e) {
            // Failure to read the stream means this is not a JAR
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return false;
    }

}
