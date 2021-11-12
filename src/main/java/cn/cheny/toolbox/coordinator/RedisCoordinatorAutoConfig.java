package cn.cheny.toolbox.coordinator;

import cn.cheny.toolbox.coordinator.redis.RedisCoordinator;
import cn.cheny.toolbox.coordinator.redis.RedisCoordinatorConstant;
import cn.cheny.toolbox.coordinator.redis.RedisCoordinatorEventListener;
import cn.cheny.toolbox.coordinator.resource.Resource;
import cn.cheny.toolbox.coordinator.resource.ResourceManager;
import cn.cheny.toolbox.other.NetUtils;
import cn.cheny.toolbox.redis.RedisConfiguration;
import cn.cheny.toolbox.redis.SpringToolboxRedisAutoConfig;
import cn.cheny.toolbox.redis.factory.RedisManagerFactory;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import cn.cheny.toolbox.spring.SpringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * redis资源协调器自动装配配置
 *
 * @author by chenyi
 * @date 2021/11/11
 */
@ConditionalOnBean(value = ResourceManager.class)
@ConditionalOnMissingBean(value = ResourceCoordinator.class)
@AutoConfigureAfter({RedisAutoConfiguration.class, SpringToolboxRedisAutoConfig.class})
public class RedisCoordinatorAutoConfig {

    @Bean
    public <T extends Resource> RedisCoordinator<T> resourceCoordinator(ResourceManager<T> resourceManager) {
        Integer port = SpringUtils.getEnvironment().getProperty("server.port", Integer.class, 8080);
        String host = NetUtils.getHost();
        RedisManagerFactory redisManagerFactory = RedisConfiguration.DEFAULT.getRedisManagerFactory();
        RedisExecutor redisExecutor = redisManagerFactory.getRedisExecutor();
        return new RedisCoordinator<>(host, port, resourceManager, redisExecutor);
    }

    @Bean
    public RedisCoordinatorEventListener redisCoordinatorEventListener(RedisCoordinator<?> resourceCoordinator) {
        return new RedisCoordinatorEventListener(resourceCoordinator);
    }

    @Bean
    public RedisMessageListenerContainer coordinatorMessageListenerContainer(RedisCoordinatorEventListener redisCoordinatorEventListener,
                                                                             RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        Map<MessageListener, Collection<? extends Topic>> messageListeners = new HashMap<>();
        messageListeners.put(redisCoordinatorEventListener, Collections.singleton(new ChannelTopic(RedisCoordinatorConstant.REDIS_CHANNEL)));
        container.setMessageListeners(messageListeners);
        return container;
    }

}
