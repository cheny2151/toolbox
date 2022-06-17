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

//    private final static Pattern IP_PATTERN = Pattern.compile("(2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2}");

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

    public static boolean isIp(String ip) {
        //todo 修改为正确的正则 return ip != null && (IP_PATTERN.matcher(ip).matches() || ip.equalsIgnoreCase("localhost"));
        return true;
    }

    public static IpAndPort parse(String text) {
        String[] split = text.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException(text + " is not a host and port");
        }
        String ip = split[0];
        if (!isIp(ip)) {
            throw new IllegalArgumentException(ip + " is not a ip");
        }
        return new IpAndPort(ip, Integer.parseInt(split[1]));
    }
}
