--
-- Created by IntelliJ IDEA.
-- User: Cheney
-- Date: 2021-11-12
-- Time: 17:04
--

-- 协调器初始化注册实例脚本
-- keys:1,心跳key;2,注册key
-- ARGV:1,心跳val;2,注册hkey;3,注册hval
redis.call('set', KEYS[1], ARGV[1])
redis.call('hset', KEYS[2], ARGV[2], ARGV[3])
return nil