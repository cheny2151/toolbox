package cn.cheny.toolbox.other;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 网络工具类
 *
 * @author by chenyi
 * @date 2021/11/10
 */
public class NetUtils {

    private final static Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    public static String getHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static boolean isLocalHost(String host) {
        return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase("localhost"));
    }

}
