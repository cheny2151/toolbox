package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.client.impl.JdkRedisClient;
import cn.cheny.toolbox.redis.client.impl.JsonRedisClient;
import cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher;
import cn.cheny.toolbox.redis.clustertask.pub.DefaultClusterTaskPublisher;
import cn.cheny.toolbox.redis.clustertask.sub.ClusterTaskDealer;
import cn.cheny.toolbox.redis.clustertask.sub.ClusterTaskRedisSub;
import cn.cheny.toolbox.redis.clustertask.sub.ClusterTaskSubscriberHolder;
import cn.cheny.toolbox.redis.factory.SpringRedisLockFactory;
import cn.cheny.toolbox.redis.lock.LockConstant;
import cn.cheny.toolbox.redis.lock.awaken.listener.SpringSubLockManager;
import cn.cheny.toolbox.spring.SpringToolAutoConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher.CLUSTER_TASK_CHANNEL_PRE_KEY;

/**
 * redis lock相关自动配置
 *
 * @author cheney
 * @date 2020-08-25
 */
@Configuration
@AutoConfigureAfter({SpringToolAutoConfig.class, RedisAutoConfiguration.class})
public class SpringRedisLockAutoConfig {

    @Bean(name = "toolbox:strRedisTemplate")
    public RedisTemplate<String, Object> strRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public JsonRedisClient jsonRedisClient(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return new JsonRedisClient<>(template);
    }

    @Bean
    public JdkRedisClient jdkRedisClient(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        return new JdkRedisClient<>(template);
    }

    @Bean(name = "toolbox:redisConfiguration")
    @DependsOn("toolbox:springUtils")
    public RedisConfiguration redisConfiguration() {
        SpringRedisLockFactory springRedisLockFactory = new SpringRedisLockFactory();
        RedisConfiguration.DEFAULT.setRedisLockFactory(springRedisLockFactory);
        return RedisConfiguration.DEFAULT;
    }

    @Bean
    public SpringSubLockManager springSubLockManager() {
        return new SpringSubLockManager();
    }

    @Bean(name = "toolbox:clusterTask", destroyMethod = "shutdown")
    public ExecutorService clusterTask() {
        return Executors.newFixedThreadPool(20);
    }

    @Bean
    public ClusterTaskPublisher clusterTaskPublisher(@Qualifier("toolbox:strRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return new DefaultClusterTaskPublisher(redisTemplate);
    }

    @Bean
    public ClusterTaskDealer clusterTaskDealer(@Qualifier("toolbox:strRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                               @Qualifier("toolbox:clusterTask") ExecutorService clusterTask) {
        return new ClusterTaskDealer(redisTemplate, clusterTask);
    }

    @Bean
    @ConditionalOnBean(name = "clusterTaskDealer")
    public ClusterTaskSubscriberHolder clusterTaskSubscriberHolder(ClusterTaskDealer clusterTaskDealer) {
        return new ClusterTaskSubscriberHolder(clusterTaskDealer);
    }

    @Bean
    @ConditionalOnBean(name = "clusterTaskSubscriberHolder")
    public ClusterTaskRedisSub clusterTaskRedisSub(ClusterTaskSubscriberHolder clusterTaskSubscriberHolder) {
        return new ClusterTaskRedisSub(clusterTaskSubscriberHolder);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       SpringSubLockManager springSubLockManager,
                                                                       ClusterTaskRedisSub clusterTaskRedisSub) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        Map<MessageListener, Collection<? extends Topic>> messageListeners = new HashMap<>();
        messageListeners.put(springSubLockManager, Collections.singleton(new PatternTopic(LockConstant.LOCK_CHANNEL + "*")));
        if (clusterTaskRedisSub != null) {
            messageListeners.put(clusterTaskRedisSub, Collections.singleton(new PatternTopic(CLUSTER_TASK_CHANNEL_PRE_KEY + "*")));
        }
        container.setMessageListeners(messageListeners);
        return container;
    }

}
