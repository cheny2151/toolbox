-- getToken
local rate_limit = redis.call('HMGET', KEYS[1], 'last_time', 'permits', 'rate', 'max_permits');
-- 上次请求时间
local last_time = rate_limit[1];
-- 剩余令牌数
local permits = rate_limit[2];
-- 生产令牌的速率
local rate = rate_limit[3];
-- 桶中最大令牌数
local max_permits = rate_limit[4];
-- 解决随机函数
redis.replicate_commands();
local cur_time = redis.call('time')[1];

local expect_permits = max_permits;

-- Redis Nil bulk reply and Nil multi bulk reply -> Lua false boolean type
if (last_time ~= false and last_time ~= nil) then
    -- 计算间隔时间可新增的令牌数
    local add_permits = (cur_time - last_time) * rate;
    if (add_permits > 0) then
        redis.call('HSET', KEYS[1], 'last_time', cur_time);
    end
    expect_permits = math.min(add_permits + permits, tonumber(max_permits));
else
    redis.call('HSET', KEYS[1], 'last_time', cur_time);
end

if (expect_permits < tonumber(ARGV[1])) then
    redis.call('HSET', KEYS[1], 'permits', expect_permits);
    return -1;
else
    redis.call('HSET', KEYS[1], 'permits', expect_permits - ARGV[1]);
    return 1;
end