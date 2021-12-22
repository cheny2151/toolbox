package cn.cheny.toolbox.coordinator.redis;

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

    private RedisCoordinator<?> redisCoordinator;

    public RedisCoordinatorEventListener(RedisCoordinator<?> redisCoordinator) {
        this.redisCoordinator = redisCoordinator;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        if (channel.equals(RedisCoordinatorConstant.REDIS_CHANNEL)) {
            String data = new String(message.getBody(), StandardCharsets.UTF_8);
            ReBalanceMessage reBalanceMessage = JSON.parseObject(data, ReBalanceMessage.class);
            String curFlag = redisCoordinator.getSid();
            if (curFlag.equals(reBalanceMessage.getSender())) {
                return;
            }
            log.info("[Coordinator] 收到广播:{}", data);
            String type = reBalanceMessage.getType();
            if (StringUtils.isNotEmpty(type)) {
                switch (type) {
                    case ReBalanceMessage.TYPE_RE_BALANCE: {
                        redisCoordinator.refreshCurrentResources();
                        break;
                    }
                    case ReBalanceMessage.TYPE_REQUIRED_RE_BALANCE: {
                        redisCoordinator.tryRebalanced();
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
