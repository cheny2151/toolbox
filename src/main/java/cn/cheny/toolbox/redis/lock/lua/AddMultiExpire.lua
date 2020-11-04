--
-- Created by IntelliJ IDEA.
-- User: Cheney
-- Date: 2020-11-04
-- Time: 16:15
--

-- 一次增加多个key的过期时间
-- KEYS:path array
-- ARGV[1]:时长

local t = ARGV[1]
for i = 1, #KEYS do
    redis.call('pexpire', KEYS[i], t)
end