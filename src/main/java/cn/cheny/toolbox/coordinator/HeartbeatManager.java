package cn.cheny.toolbox.coordinator;

import java.io.Closeable;

/**
 * 资源协调器心跳接口
 *
 * @author by chenyi
 * @date 2021/12/21
 */
public interface HeartbeatManager extends Closeable {

    /**
     * 获取当前节点sid
     *
     * @return 当前节点sid
     */
    String getSid();

    /**
     * 判断实例是否存活
     *
     * @param sid 实例id
     * @return 实例是否存活
     */
    boolean isActive(String sid);

    /**
     * 检查当前实例心跳
     * 1.是否已注册心跳
     * 2.未注册则重启心跳线程
     */
    void checkHeartbeat();

}
