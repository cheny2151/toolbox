--
-- Created by IntelliJ IDEA.
-- User: Cheney
-- Date: 2020-03-17
-- Time: 17:04
--

-- 多路径锁解锁脚本
-- keys:1,路径set的key;2,channel路径
-- ARGV:1,channel信息;剩余的为路径值，需将路径值存放到set中
-- 返回：1,代表目前路径解锁成功/已经解锁;null代表未持有该锁
if (redis.call('exists', KEYS[1]) == 0) then
    redis.call('publish', KEYS[2], ARGV[1]);
    return 1;
end
for i = 2, #ARGV do
    redis.call('srem', KEYS[1], ARGV[i]);
end
redis.call('publish', KEYS[2], ARGV[1]);
return 1;