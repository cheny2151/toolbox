TOOLBOX
===========================
提供常用工具类,redis分布式锁，redis分布式任务发布订阅，注解实体缓存，表达式解析器，poi工具类封装，反射工具，包扫描器，多线程任务等。并且在Spring boot环境下，自动配置。

****
## 目录
* [redis 分布式锁](#Redis分布式锁)
    * [锁实现类](#锁实现类)
    * [Spring环境下用例](#分布式锁Spring环境下用例)
    * [非Spring环境下用例（jedis）](#分布式锁非Spring环境下用例（jedis）)
* [redis分布式任务发布订阅](#Redis分布式集群任务)
    * [Spring环境下用例](#集群任务Spring环境下用例)
    * [非Spring环境下用例（jedis）](#集群任务非Spring环境下用例（jedis）)
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
    特点：一级锁被占用，则任务二级锁获取失败；二级锁被占用，则一级锁获取失败，但不会阻塞其他线程获取其他二级锁。

### 分布式锁Spring环境下用例
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
注意：
RedisLock继承了AutoCloseable，在try中创建实例会自动调用Close释放锁。

### 分布式锁非Spring环境下用例（jedis）
非Spring环境下，需要配置jedis工厂、将JedisManagerFactory实例配置到RedisConfiguration中即可使用。
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
Redis分布式集群任务
-----------
### 集群任务Spring环境下用例
Spring环境静下，无需其他配置
1. 任务发布 -- ClusterTaskPublisher <br/>
    发布任务api
    ```java
    /**
     * 发布集群任务
     *
     * @param taskId         任务ID
     * @param dataNums       待处理数据个数
     * @param stepSize       步长
     * @param concurrentNums 单个服务器并发数量
     * @param desc           是否倒序
     * @param header         任务头部信息
     */
    publish(taskId, dataNums, stepSize, concurrentNums, desc, header);
    ```
    发布任务实例:
    ```java
    // 注入任务发布者
    @Resource(name = "clusterTaskPublisher")
    private ClusterTaskPublisher clusterTaskPublisher;
    
    // 调用sql查询待处理数据量
    int dataNums = dataMapper.countData();
    // 发布了一个数据量为dataNums的任务，指定一个分片（分页）100条数据并且倒序分配，并且指定订阅者开启6条线程执行任务
    clusterTaskPublisher.publish("test", dataNums, 100, 6, true, null);
    ```
2. 任务订阅 -- 继承AbstractClusterTaskSubscriber <br/>
    可由多个服务器节点订阅任务信息，多个节点多线程执行任务，充分利用服务器资源。
    ```java
    @Component
    @SubTask(taskId = "test")
    public class TestSub extends AbstractClusterTaskSubscriber {
    
        @Override
        public void subscribe(TaskInfo taskInfo, Limit limit) {
            // limit为分页的封装对象，num即为分页开始数,size即为分页大小,
            // 假设前面发布了一个300条数据量的任务，由于前面发布的任务是倒序，
            // 那么第一次执行任务的num为200,size为100，第二个为{100,100}，第三个为{0,100}。
            dataMapper.queryData(limit.getNum,limit.getSize);
        }
        
       /**
        * 所有分片任务执行完将回调此方法
        */
        @Override
        public void afterAllTask() {
            System.out.println("finish task");
        }
    
    }
    ```
3. 特殊用法 -- 利用header构造任务 <br/>
    ```java
    Map<String, Object> header = new HashMap<>();
    ArrayList<String> list = new ArrayList<>();
    list.add("task1");
    list.add("task2");
    list.add("task3");
    list.add("task4");
    list.add("task5");
    list.add("task6");
    header.put("taskEnum", list);
    clusterTaskPublisher.publish("test2", list.size(), 1, 1, true, header);
    return JsonMessage.success("123");
   
   // 订阅方法
   @Override
   public void subscribe(TaskInfo taskInfo, Limit limit) {   
       Map<String, Object> header = taskInfo.getHeader();
       ArrayList<String> taskEnum = (List<String>)header.get("taskEnum");
       int num = limit.getNum();
       String taskEnum = taskEnum.get(num);
       swtich(taskEnum){
           // 捕获不同的任务枚举，执行不同的任务
           case "task1": ...
           case "task2": ...
           case "task3": ...
           case "task4": ...
           case "task5": ...
           case "task6": ...
       }   
   }
    ```
### 集群任务非Spring环境下用例（jedis）
1. 配置并初始化 <br/>
    与配置redis分布式锁类型，但需要调用JedisClusterHelper.initJedisCluster(factory)初始化任务发布订阅器。
    ```Java
    JedisClientFactory factory = new JedisClientFactory("localhost", null, null, null);
    JedisClient jedisClient = factory.cacheClient();
    RedisConfiguration.setDefaultRedisManagerFactory(new JedisManagerFactory(jedisClient));
    ClusterTaskPublisher clusterTaskPublisher = JedisClusterHelper.initJedisCluster(factory);
    clusterTaskPublisher.publish("test", 100, 10, 1, true);
    Thread.sleep(10000);
    ```
    其他与spring环境下使用情况一致。<br>

// TODO