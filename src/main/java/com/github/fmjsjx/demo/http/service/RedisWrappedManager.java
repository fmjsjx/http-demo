package com.github.fmjsjx.demo.http.service;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.core.model.RedisLock;
import com.github.fmjsjx.libcommon.redis.RedisUtil;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;

public abstract class RedisWrappedManager implements RedisWrapped<String, String> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("globalRedisConnection")
    private StatefulRedisConnection<String, String> globalRedisConnection;
    @Autowired
    @Qualifier("loggingRedisConnection")
    private StatefulRedisConnection<String, String> loggingRedisConnection;
    @Autowired
    @Qualifier("activityRedisConnection")
    private StatefulRedisConnection<String, String> activityRedisConnection;

    @Override
    public StatefulRedisConnection<String, String> globalRedisConnection() {
        return globalRedisConnection;
    }

    @Override
    public StatefulRedisConnection<String, String> loggingRedisConnection() {
        return loggingRedisConnection;
    }

    @Override
    public StatefulRedisConnection<String, String> activityRedisConnection() {
        return activityRedisConnection;
    }

    public Long nextId(String field) {
        return nextId(field, 1);
    }

    public Long nextId(String field, long amount) {
        var key = KEY_IDENTITY_MAP;
        logger.debug("[redis:global] HINCRBY {} {} {}", key, field, amount);
        return globalRedisSync().hincrby(key, field, amount);
    }

    public RedisFuture<Long> nextIdAsync(String field) {
        return nextIdAsync(field, 1);
    }

    public RedisFuture<Long> nextIdAsync(String field, long amount) {
        var key = KEY_IDENTITY_MAP;
        logger.debug("[redis:global] HINCRBY {} {} {}", key, field, amount);
        return globalRedisAsync().hincrby(key, field, amount);
    }

    public Optional<RedisLock> tryLock(String key, String value, int timeout) {
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        var ok = RedisUtil.tryLock(globalRedisSync(), key, value, timeout);
        return toGlobalLock(key, value, ok);
    }

    protected RedisLock toGlobalLock(String key, String value) {
        return new RedisLock(key, value, this::globalRedisConnection, "global");
    }

    protected Optional<RedisLock> toGlobalLock(String key, String value, boolean ok) {
        if (ok) {
            return Optional.of(toGlobalLock(key, value));
        }
        return Optional.empty();
    }

    public CompletionStage<Optional<RedisLock>> tryLockAsync(String key, String value, int timeout) {
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        return RedisUtil.tryLock(globalRedisAsync(), key, value, timeout).thenApply(ok -> toGlobalLock(key, value, ok));
    }

    public RedisLock lock(String key, int timeout, long maxWait) {
        return tryLock(key, timeout, maxWait).orElseThrow(ApiErrors::clickTooQuick);
    }

    public RedisLock lock(String key, String value, int timeout, long maxWait) {
        return tryLock(key, value, timeout, maxWait).orElseThrow(ApiErrors::clickTooQuick);
    }

    public Optional<RedisLock> tryLock(String key, int timeout, long maxWait) {
        var value = Long.toString(System.currentTimeMillis(), 36) + Long.toString(System.nanoTime(), 36);
        return tryLock(key, value, timeout, maxWait);
    }

    public Optional<RedisLock> tryLock(String key, String value, int timeout, long maxWait) {
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        try {
            var ok = RedisUtil.tryLock(globalRedisSync(), key, value, timeout, maxWait);
            return toGlobalLock(key, value, ok);
        } catch (InterruptedException e) {
            // ignore
        }
        return Optional.empty();
    }

    public CompletionStage<Optional<RedisLock>> tryLockAsync(String key, String value, int timeout, long maxWait) {
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        return RedisUtil.tryLock(globalRedisAsync(), key, value, timeout, maxWait)
                .thenApply(ok -> toGlobalLock(key, value, ok));
    }

}
