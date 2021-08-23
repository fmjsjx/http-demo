package com.github.fmjsjx.demo.http.core.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.fmjsjx.libcommon.redis.LuaScripts;
import com.github.fmjsjx.libcommon.redis.RedisUtil;

import io.lettuce.core.api.StatefulRedisConnection;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class RedisLock {

    private final String key;
    private final String value;
    @ToString.Exclude
    private final Supplier<StatefulRedisConnection<String, String>> connSupplier;
    @ToString.Exclude
    private final String redisName;

    private final AtomicBoolean unlocked = new AtomicBoolean();

    public RedisLock(String key, String value, Supplier<StatefulRedisConnection<String, String>> connSupplier,
            String redisName) {
        this.key = key;
        this.value = value;
        this.connSupplier = connSupplier;
        this.redisName = redisName;
    }

    public boolean unlocked() {
        return unlocked.get();
    }

    public RedisLock unlock() {
        if (unlocked.compareAndSet(false, true)) {
            String[] keys = { key };
            var value = this.value;
            var script = LuaScripts.DEL_IF_VALUE_EQUALS;
            log.debug("[redis:{}] EVAL {} {} {}", redisName, script, keys, value);
            RedisUtil.eval(connSupplier.get().async(), script, keys, value);
        }
        return this;
    }

    public CompletionStage<Boolean> unlockAsync() {
        if (unlocked.compareAndSet(false, true)) {
            String[] keys = { key };
            var value = this.value;
            var script = LuaScripts.DEL_IF_VALUE_EQUALS;
            log.debug("[redis:{}] EVAL {} {} {}", redisName, script, keys, value);
            return RedisUtil.eval(connSupplier.get().async(), script, keys, value);
        }
        return CompletableFuture.completedStage(Boolean.FALSE);
    }

    public void runThenUnlock(Runnable action) {
        runThenUnlock(action, false);
    }

    public void runThenUnlock(Runnable action, boolean validate) {
        if (unlocked() && validate) {
            throw new IllegalStateException("this lock already unlocked");
        }
        try {
            action.run();
        } finally {
            unlock();
        }
    }

    public CompletionStage<Void> runThenUnlockAsync(CompletionStage<Void> future) {
        return future.whenComplete((r, e) -> unlocked());
    }

    public <R> R supplyThenUnlock(Supplier<R> supplier) {
        if (unlocked()) {
            throw new IllegalStateException("this lock already unlocked");
        }
        try {
            return supplier.get();
        } finally {
            unlock();
        }
    }

    public <R> CompletionStage<R> supplyThenUnlockAsync(CompletionStage<R> future) {
        return future.whenComplete((r, e) -> unlock());
    }

}
