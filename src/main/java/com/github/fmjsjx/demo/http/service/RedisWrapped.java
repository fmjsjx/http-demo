package com.github.fmjsjx.demo.http.service;

import com.github.fmjsjx.libcommon.redis.LuaScript;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

public interface RedisWrapped<K, V> {

    LuaScript<Long> INCREX_SCRIPT = LuaScript.forNumber(
            "local n = redis.call('incr', KEYS[1]) if n == 1 then redis.call('expire', KEYS[1], ARGV[1]) end return n");

    LuaScript<Boolean> SADDEX_SCRIPT = LuaScript.forBoolean(
            "local n = redis.call('sadd', KEYS[1], ARGV[1]) if redis.call('ttl', KEYS[1]) == -1 then redis.call('expire', KEYS[1], ARGV[2]) end return n");

    static String KEY_IDENTITY_MAP = "identity:map";

    StatefulRedisConnection<K, V> globalRedisConnection();

    default RedisCommands<K, V> globalRedisSync() {
        return globalRedisConnection().sync();
    }

    default RedisAsyncCommands<K, V> globalRedisAsync() {
        return globalRedisConnection().async();
    }

    StatefulRedisConnection<K, V> loggingRedisConnection();

    default RedisCommands<K, V> loggingRedisSync() {
        return loggingRedisConnection().sync();
    }

    default RedisAsyncCommands<K, V> loggingRedisAsync() {
        return loggingRedisConnection().async();
    }

    StatefulRedisConnection<K, V> activityRedisConnection();

    default RedisCommands<K, V> activityRedisSync() {
        return activityRedisConnection().sync();
    }

    default RedisAsyncCommands<K, V> activityRedisAsync() {
        return activityRedisConnection().async();
    }

}
