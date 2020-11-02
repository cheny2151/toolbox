TOOLBOX
===========================
提供常用工具类,redis分布式锁，redis分布式任务发布订阅，注解实体缓存，表达式解析器，poi工具类封装，反射工具，包扫描器，多线程任务等。并且在Spring boot环境下，自动配置。

****
## 目录
* [redis 分布式锁](#Redis分布式锁)
    * [锁实现类](#锁实现类)
    * Spring环境下用例
    * 非Spring环境下用例（jedis）
* redis分布式任务发布订阅
    * Spring环境下用例
    * 非Spring环境下用例（jedis）
* 注解实体缓存
    * mybatis环境下使用
    * ...
* 表达式解析器
    * 导入静态方法
    * 测试表达式
    * 用例
-----------

Redis分布式锁
-----------
### 锁实现类
1. 可重入锁 -- ReentrantRedisLock<br/>
    特点：可重入
2. 多路径锁 -- MultiPathRedisLock<br/>
    特点：一次性可对多个path上锁，只要有一个path被其他线程持有，则获取锁失败
3. 二级锁 -- SecondLevelRedisLock<br/>
    特点：一级锁被占用，则任务二级锁获取失败；二级锁被占用则，一级锁获取失败，但不会阻塞其他线程获取其他二级锁。

### Spring环境下用例
Spring环境静下，无需其他配置，自动整合spring-boot-starter-data-redis，通过redisTemplate的api实现redis操作。
```Java
try (RedisLock redisLock = new ReentrantRedisLock("test")) {
    if (redisLock.tryLock(1000, 1000, TimeUnit.MILLISECONDS)) {
        System.out.println("获取锁成功");
    }
} catch (Exception e) {
    e.printStackTrace();
}
```
RedisLock继承了AutoCloseable，在try中创建实例会自动调用Close释放锁。

### 非Spring环境下用例（jedis）
```Java
// 配置jedis工厂
JedisClientFactory factory = new JedisClientFactory("localhost", null, null, null);
// 从工厂中获取jedisClient
JedisClient jedisClient = factory.cacheClient();
JedisManagerFactory jedisLockFactory = new JedisManagerFactory(jedisClient);
// 设置全局redis manager为jedis实现类
RedisConfiguration.setDefaultRedisManagerFactory(jedisLockFactory);
// 完成初始化，之后可在任意位置直接使用：
try (RedisLock redisLock = new ReentrantRedisLock("test")) {
    if (redisLock.tryLock(1000, 1000, TimeUnit.MILLISECONDS)) {
        System.out.println("获取锁成功");
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

// TODO