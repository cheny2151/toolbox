--
-- Created by IntelliJ IDEA.
-- User: Cheney
-- Date: 2021-11-12
-- Time: 17:04
--

-- 协调器初始化注册实例脚本
-- keys:1,心跳key;2,注册key;3,注册hkey
-- ARGV:1,心跳val;2,注册hval
redis.call('set', KEYS[1],ARGV[1]))
redis.call('hset', KEYS[2], KEYS[3], ARGV[2])
return nil