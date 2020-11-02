package cn.cheny.toolbox.redis;

/**
 * redis集群key工具类
 *
 * @author cheney
 * @date 2020-10-14
 */
public class RedisKeyUtils {

    private RedisKeyUtils() {
    }

    public static String generatedSafeKey(String pre, String core, String post) {
        StringBuilder keyBuilder = pre == null ? new StringBuilder() : new StringBuilder(pre);
        keyBuilder.append("{").append(core).append("}");
        if (post != null) {
            keyBuilder.append(post);
        }
        return keyBuilder.toString();
    }

    public static String splitKeyCore(String key) {
        return key.substring(key.indexOf("{") + 1, key.indexOf("}"));
    }

}
