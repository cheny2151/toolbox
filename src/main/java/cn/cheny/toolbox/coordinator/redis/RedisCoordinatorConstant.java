package cn.cheny.toolbox.coordinator.redis;

import org.apache.commons.lang3.StringUtils;

/**
 * redis协调器常量/脚本
 *
 * @author by chenyi
 * @date 2021/11/12
 */
public enum RedisCoordinatorConstant {

    REDIS_CHANNEL("{COORDINATOR%s}", "RE_BALANCE:CHANNEL"),
    RESOURCES_REGISTER("{COORDINATOR%s}", "REGISTER"),
    RE_BALANCE_LOCK("COORDINATOR%s", "RE_BALANCE:LOCK"),
    HEART_BEAT_KEY_PRE("{COORDINATOR%s}", "HEART_BEAT");

    public static final String HEARTBEAT_VAL = "1";
    public static final String KEY_SPLIT = ":";
    private static final String ID_CONNECT = "_";

    private String keyBase;
    private String keyBody;

    RedisCoordinatorConstant(String keyBase, String keyBody) {
        this.keyBase = keyBase;
        this.keyBody = keyBody;
    }

    public String buildKey(String id) {
        if (StringUtils.isNotEmpty(id)) {
            id = ID_CONNECT + id;
        }
        String key = String.format(this.keyBase, ID_CONNECT + id);
        return key + KEY_SPLIT + keyBody;
    }

}
