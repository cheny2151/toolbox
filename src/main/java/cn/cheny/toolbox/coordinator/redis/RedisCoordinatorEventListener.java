package cn.cheny.toolbox.coordinator.redis;

import cn.cheny.toolbox.coordinator.CoordinatorHolder;
import cn.cheny.toolbox.coordinator.CoordinatorProperty;
import cn.cheny.toolbox.coordinator.HeartbeatManager;
import cn.cheny.toolbox.coordinator.ResourceCoordinator;
import cn.cheny.toolbox.coordinator.msg.ReBalanceMessage;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

/**
 * redis资源协调器事件监听器
 *
 * @author by chenyi
 * @date 2021/11/10
 */
@Slf4j
public class RedisCoordinatorEventListener implements MessageListener {

    private final CoordinatorHolder coordinatorHolder;
    private final String channelId;
    private final String sid;

    public RedisCoordinatorEventListener(CoordinatorProperty coordinatorProperty, HeartbeatManager heartBeatManager, CoordinatorHolder coordinatorHolder) {
        this.coordinatorHolder = coordinatorHolder;
        this.channelId = RedisCoordinatorConstant.REDIS_CHANNEL.buildKey(coordinatorProperty.getId());
        this.sid = heartBeatManager.getSid();
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        if (channel.equals(channelId)) {
            String data = new String(message.getBody(), StandardCharsets.UTF_8);
            ReBalanceMessage reBalanceMessage = JSON.parseObject(data, ReBalanceMessage.class);
            String resourceKey = reBalanceMessage.getResourceKey();
            ResourceCoordinator<?> coordinator = coordinatorHolder.get(resourceKey);
            if (coordinator != null) {
                if (sid.equals(reBalanceMessage.getSender())) {
                    return;
                }
                log.info("[Coordinator] 收到广播:{}", data);
                String type = reBalanceMessage.getType();
                if (StringUtils.isNotEmpty(type)) {
                    switch (type) {
                        case ReBalanceMessage.TYPE_RE_BALANCE: {
                            coordinator.refreshCurrentResources();
                            break;
                        }
                        case ReBalanceMessage.TYPE_REQUIRED_RE_BALANCE: {
                            coordinator.tryRebalanced();
                            break;
                        }
                        default: {
                            log.warn("[Coordinator] 未知的redis channel信息type:{}", type);
                        }
                    }
                }
            }
        }
    }

}
