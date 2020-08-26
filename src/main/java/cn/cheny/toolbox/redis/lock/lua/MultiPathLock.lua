--
-- Created by IntelliJ IDEA.
-- User: Cheney
-- Date: 2020-03-17
-- Time: 16:15
--

-- 多路径锁上锁脚本
-- keys:1,路径set的key
-- ARGV:1,过期时间;剩余的为路径值，需将路径值存放到set中
-- 返回：null代表上锁成功;数值为过期时间
if (redis.call('exists', KEYS[1]) == 0) then
    for i = 2, #ARGV do
        redis.call('sadd', KEYS[1], ARGV[i]);
    end
    if (tonumber(ARGV[1]) > 0) then
        redis.call('pexpire', KEYS[1], ARGV[1]);
    end
    return nil;
end

local ex = 0;
for i = 2, #ARGV do
    if (redis.call('sismember', KEYS[1], ARGV[i]) == 1) then
        ex = 1;
        break;
    end
end
if (ex == 0) then
    for i = 2, #ARGV do
        redis.call('sadd', KEYS[1], ARGV[i]);
    end
    if (tonumber(ARGV[1]) > 0) then
        redis.call('pexpire', KEYS[1], ARGV[1]);
    end
    return nil;
else
    return redis.call('pttl', KEYS[1]);
end