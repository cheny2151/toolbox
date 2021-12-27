package cn.cheny.toolbox.scan;

import static cn.cheny.toolbox.scan.PathScanner.PACKAGE_SEPARATE_CHARACTER;
import static cn.cheny.toolbox.scan.PathScanner.URL_SEPARATE_CHARACTER;

/**
 * @author by chenyi
 * @date 2021/12/27
 */
public class PackageUtils {

    public static String replaceUrlToPackage(String url) {
        return url.replaceAll(URL_SEPARATE_CHARACTER, PACKAGE_SEPARATE_CHARACTER);
    }

    public static String replacePackageToUrl(String pack) {
        return pack.replaceAll("\\.", URL_SEPARATE_CHARACTER);
    }

}
