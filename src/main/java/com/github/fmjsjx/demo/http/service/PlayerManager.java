package com.github.fmjsjx.demo.http.service;

import static com.github.fmjsjx.demo.http.api.Constants.Events.CROSS_DAY;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.core.config.ServerConfig;
import com.github.fmjsjx.demo.http.core.config.ServerConfig.CacheMode;
import com.github.fmjsjx.demo.http.core.config.ServerConfig.SystemConfig;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.core.model.RedisLock;
import com.github.fmjsjx.demo.http.core.model.ServiceContext;
import com.github.fmjsjx.demo.http.entity.model.Player;
import com.github.fmjsjx.demo.http.exception.ConcurrentlyUpdateException;
import com.github.fmjsjx.demo.http.sdk.PartnerUserInfo;
import com.github.fmjsjx.demo.http.util.ConfigUtil;
import com.github.fmjsjx.libcommon.json.Jackson2Library;
import com.github.fmjsjx.libcommon.redis.RedisUtil;
import com.github.fmjsjx.libcommon.util.RandomUtil;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import io.lettuce.core.RedisFuture;

@Service
public class PlayerManager extends RedisWrappedManager {

    protected static final String LOCK_SCOPE_GLOBAL = "global";
    protected static final String KEY_BASIC_INFO_MAP = "basic-info:map";

    private static final long CACHE_TTL = 1800;
    private static final String CACHE_TTL_STR = String.valueOf(CACHE_TTL);

    public static final String generateGuestNickname() {
        return "玩家" + RandomUtil.randomInRange(1_000_000_000, 1_999_999_999);
    }

    private static final String toRedisLockKey(int uid, String scope) {
        return "lock:player:{" + uid + "}:" + scope;
    }

    private static final String toCachePlayerDataKey(int uid) {
        return "cache:player:{" + uid + "}:data";
    }

    private static final String generateLockValue(int uid) {
        return Long.toString(System.currentTimeMillis(), 36);
    }

    public static final void increaseGamingCount(Player player) {
        player.getStatistics().increaseGamingCount();
        var dailyGamingCount = player.getDaily().increaseGamingCount();
        if (dailyGamingCount == 1) {
            // 当日首次
            var login = player.getLogin();
            var gamingDays = login.increaseGamingDays();
            if (gamingDays > login.getMaxGamingDays()) {
                login.setMaxGamingDays(gamingDays);
            }
        }
    }

    private static final SystemConfig systemConfig() {
        return ServerConfig.getInstance().system();
    }

    private static final Player fromJsonData(String value) {
        var player = new Player();
        player.load(Jackson2Library.getInstance().loads(value));
        return player;
    }

    private static final String toJsonData(Player player) {
        return Jackson2Library.getInstance().dumpsToString(player.toData());
    }

    @Autowired
    private ConfigManager configManager;
    @Autowired
    private MongoDBManager mongoDBManager;
    @Autowired
    private BusinessLogManager businessLogManager;

    private MongoCollection<BsonDocument> playerBsonCollection(int groupId) {
        return mongoDBManager.gameDatabase(groupId).getCollection("player", BsonDocument.class);
    }

    private Optional<BsonDocument> findOne(int groupId, Bson filter) {
        try {
            return Optional.ofNullable(playerBsonCollection(groupId).find(filter).first());
        } catch (Exception e) {
            logger.error("[mongodb:player] Find one player failed: {}", filter, e);
            throw ApiErrors.dataAccessError(e);
        }
    }

    public Optional<Player> findPlayer(int groupId, int uid) {
        return findOne(groupId, eq(uid)).map(this::loadPlayer);
    }

    public Player getPlayer(AuthToken token) {
        var system = systemConfig();
        if (system.usePlayerCache()) {
            var cacheMode = system.playerCacheMode();
            if (cacheMode == CacheMode.LOCAL) {
                return getPlayerUseLocalCache(token, system);
            }
            if (cacheMode == CacheMode.REDIS) {
                return getPlayerUseRedisCache(token, system);
            }
        }
        return findPlayer(token.gid(), token.uid()).orElseThrow(ApiErrors::dataAccessError);
    }

    private Player getPlayerUseRedisCache(AuthToken token, SystemConfig system) {
        var key = toCachePlayerDataKey(token.uid());
        var value = globalRedisSync().get(key);
        logger.debug("[redis:global] GET {} <<< {}", key, value);
        if (value != null) {
            try {
                return fromJsonData(value);
            } catch (Exception e) {
                logger.warn("[redis:global] Parse player data failed: {} <<< {}", key, value, e);
                logger.debug("[redis:global] DEL {} ", key);
                globalRedisAsync().del(key);
            }
        }
        var bson = findOne(token.gid(), eq(token.uid())).orElseThrow(ApiErrors::dataAccessError);
        var player = loadPlayer(bson);
        cacheInRedisAsync(key, toJsonData(player));
        return player;
    }

    private Player getPlayerUseLocalCache(AuthToken token, SystemConfig system) {
        Optional<Player> playerLocalCache = token.property(Player.class);
        if (playerLocalCache.isPresent()) {
            var player = playerLocalCache.get();
            player.reset();
            return player;
        } else {
            if (system.playerSyncForcedly()) {
                fillPlayerLocalCache(token);
                throw ConcurrentlyUpdateException.getInstance();
            }
            return fillPlayerLocalCache(token);
        }
    }

    public Player fillPlayerLocalCache(AuthToken token) {
        var player = findPlayer(token.gid(), token.uid()).orElseThrow(ApiErrors::dataAccessError);
        token.setProperty(Player.class, player);
        logger.debug("[cache:player] Fill cache: {} => {}", player, token);
        return player;
    }

    public Player getPlayer(ServiceContext ctx) {
        var player = getPlayer(ctx.token());
        ctx.player(player);
        fixPlayerBeforeProcessing(ctx);
        return player;
    }

    private Player loadPlayer(BsonDocument src) {
        var player = new Player();
        player.load(src);
        return player;
    }

    private Player createPlayer(AuthToken token, String nickname, String faceUrl) {
        var player = new Player();
        player.setUid(token.uid());
        initPlayer(token, nickname, faceUrl, player);
        var now = LocalDateTime.now();
        player.setCreateTime(now);
        player.setUpdateTime(now);
        player.reset();
        // insert one into MongoDB
        try {
            playerBsonCollection(token.gid()).insertOne(player.toBson());
        } catch (Exception e) {
            logger.error("[mongodb:player] Insert one player failed: {}", player, e);
            throw ApiErrors.dataAccessError(e);
        }
        storeBasicInfo(player);
        return player;
    }

    private void initPlayer(AuthToken token, String nickname, String faceUrl, Player player) {
        var config = configManager.playerInitShard(token);
        // references
        player.getPreferences().setCustom("");
        // basic
        var basic = player.getBasic();
        if (nickname != null) {
            basic.setNickname(nickname);
        } else {
            basic.setNickname("");
        }
        if (faceUrl != null) {
            basic.setFaceUrl(faceUrl);
        } else {
            basic.setFaceUrl("");
        }
        // login
        var login = player.getLogin();
        login.setCount(1);
        login.setDays(1);
        login.setContinuousDays(1);
        login.setMaxContinuousDays(1);
        login.setLoginTime(token.getLoginTime());
        login.setIp(token.getIp());
        // guide
        // wallet
        var wallet = player.getWallet();
        wallet.setCoinTotal(config.coin());
        wallet.setDiamond(config.diamond());
        // items
        var items = player.getItems();
        for (var item : config.items()) {
            items.put(item.getItem(), item.getNum());
        }
        // videos
        // cattleRoping
        // daily
        var daily = player.getDaily();
        daily.setDay(token.getLoginTime().toLocalDate());
    }

    private void storeBasicInfo(Player player) {
        if (player.getBasic() != null) {
            var key = KEY_BASIC_INFO_MAP;
            var field = Integer.toString(player.getUid());
            var value = Jackson2Library.getInstance().dumps(player.getBasic());
            logger.debug("[redis:global] HSET {} {} {}", key, field, value);
            globalRedisAsync().hset(key, field, value);
        }
    }

    public Player getGuestPlayer(AuthToken token) {
        // create automatically if not persistent
        return findPlayer(token.gid(), token.uid()).orElseGet(() -> createGuestPlayer(token));
    }

    public Player createGuestPlayer(AuthToken token) {
        try {
            return createPlayer(token, generateGuestNickname(), "");
        } catch (DuplicateKeyException e) {
            return getGuestPlayer(token);
        }
    }

    public Player getPlayer(AuthToken token, PartnerUserInfo user) {
        // create automatically if not persistent
        return findPlayer(token.gid(), token.uid()).orElseGet(() -> createPlayer(token, user));
    }

    public Player createPlayer(AuthToken token, PartnerUserInfo user) {
        try {
            return createPlayer(token, user.nickname(), user.faceUrl());
        } catch (DuplicateKeyException e) {
            return getPlayer(token, user);
        }
    }

    public Optional<RedisLock> tryLock(int uid, int timeout) {
        return tryLock(uid, LOCK_SCOPE_GLOBAL, timeout);
    }

    public Optional<RedisLock> tryLock(int uid, String scope, int timeout) {
        var key = toRedisLockKey(uid, scope);
        var value = generateLockValue(uid);
        return tryLock(key, value, timeout);
    }

    public Optional<RedisLock> tryLock(int uid, int timeout, long maxWait) {
        return tryLock(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait);
    }

    public Optional<RedisLock> tryLock(int uid, String scope, int timeout, long maxWait) {
        var key = toRedisLockKey(uid, scope);
        var value = generateLockValue(uid);
        return tryLock(key, value, timeout, maxWait);
    }

    public CompletionStage<Optional<RedisLock>> tryLockAsync(int uid, int timeout, Executor executor) {
        var key = toRedisLockKey(uid, LOCK_SCOPE_GLOBAL);
        var value = generateLockValue(uid);
        return tryLockAsync(key, value, timeout);
    }

    public CompletionStage<Optional<RedisLock>> tryLockAsync(int uid, String scope, int timeout, Executor executor) {
        var key = toRedisLockKey(uid, scope);
        var value = generateLockValue(uid);
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        return RedisUtil.tryLock(globalRedisAsync(), key, value, timeout)
                .thenApplyAsync(ok -> toGlobalLock(key, value, ok), executor);
    }

    public RedisLock lock(int uid, int timeout) {
        return lock(uid, LOCK_SCOPE_GLOBAL, timeout);
    }

    public RedisLock lock(int uid, String scope, int timeout) {
        return lock(uid, scope, timeout, ApiErrors::clickTooQuick);
    }

    public RedisLock lock(int uid, int timeout, Supplier<? extends RuntimeException> exceptionSupplier) {
        return lock(uid, LOCK_SCOPE_GLOBAL, timeout, exceptionSupplier);
    }

    public RedisLock lock(int uid, String scope, int timeout, Supplier<? extends RuntimeException> exceptionSupplier) {
        return tryLock(uid, scope, timeout).orElseThrow(exceptionSupplier);
    }

    public RedisLock lock(int uid, int timeout, long maxWait) {
        return lock(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait);
    }

    public RedisLock lock(int uid, int timeout, long maxWait, Supplier<? extends RuntimeException> exceptionSupplier) {
        return lock(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait, exceptionSupplier);
    }

    public RedisLock lock(int uid, String scope, int timeout, long maxWait) {
        return lock(uid, scope, timeout, maxWait, ApiErrors::clickTooQuick);
    }

    public RedisLock lock(int uid, String scope, int timeout, long maxWait,
            Supplier<? extends RuntimeException> exceptionSupplier) {
        return tryLock(uid, scope, timeout).orElseThrow(exceptionSupplier);
    }

    public CompletionStage<RedisLock> lockAsync(int uid, int timeout, long maxWait) {
        return lockAsync(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait, ApiErrors::clickTooQuick);
    }

    public CompletionStage<RedisLock> lockAsync(int uid, int timeout, long maxWait,
            Supplier<? extends RuntimeException> exceptionSupplier) {
        return lockAsync(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait, exceptionSupplier);
    }

    public CompletionStage<RedisLock> lockAsync(int uid, String scope, int timeout, long maxWait,
            Supplier<? extends RuntimeException> exceptionSupplier) {
        var key = toRedisLockKey(uid, scope);
        var value = generateLockValue(uid);
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        return RedisUtil.tryLock(globalRedisAsync(), key, value, timeout, maxWait).thenApply(ok -> {
            if (ok) {
                return toGlobalLock(key, value);
            }
            throw exceptionSupplier.get();
        });
    }

    public CompletionStage<RedisLock> lockAsync(int uid, int timeout, long maxWait, Executor executor) {
        return lockAsync(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait, executor, ApiErrors::clickTooQuick);
    }

    public CompletionStage<RedisLock> lockAsync(int uid, int timeout, long maxWait, Executor executor,
            Supplier<? extends RuntimeException> exceptionSupplier) {
        return lockAsync(uid, LOCK_SCOPE_GLOBAL, timeout, maxWait, executor, exceptionSupplier);
    }

    public CompletionStage<RedisLock> lockAsync(int uid, String scope, int timeout, long maxWait, Executor executor,
            Supplier<? extends RuntimeException> exceptionSupplier) {
        var key = toRedisLockKey(uid, scope);
        var value = generateLockValue(uid);
        logger.debug("[redis:global] SET {} {} NX EX {}", key, value, timeout);
        return RedisUtil.tryLock(globalRedisAsync(), key, value, timeout, maxWait, executor).thenApplyAsync(ok -> {
            if (ok) {
                return toGlobalLock(key, value);
            }
            throw exceptionSupplier.get();
        }, executor);
    }

    public <R> R autoRetry(IntFunction<R> action) {
        return autoRetry(action, ApiErrors::dataAccessError);
    }

    public <R> R autoRetry(IntFunction<R> action, Supplier<? extends RuntimeException> exceptionSupplier) {
        for (var retryCount = 0; retryCount <= ConfigUtil.retryCount(); retryCount++) {
            try {
                return action.apply(retryCount);
            } catch (ConcurrentlyUpdateException e) {
                // auto retry when meet ConcurrentlyUpdateException
            }
        }
        throw exceptionSupplier.get();
    }

    public void update(ServiceContext ctx) throws ConcurrentlyUpdateException {
        var token = ctx.token();
        var system = systemConfig();
        if (updateCas(ctx)) {
            businessLogManager.logEventsAsync(ctx.eventLogs());
            if (system.usePlayerCache()) {
                if (system.playerCacheMode() == CacheMode.REDIS) {
                    // update REDIS cache when update success
                    var key = toCachePlayerDataKey(token.uid());
                    var value = toJsonData(ctx.player());
                    cacheInRedisAsync(key, value);
                } else if (system.playerCacheMode() == CacheMode.LOCAL) {
                    token.setProperty(Player.class, ctx.player());
                }
            }
        } else {
            // always clear local cache when update failed
            token.removeProperty(Player.class, ctx.player());
            if (system.usePlayerCache()) {
                if (system.playerCacheMode() == CacheMode.REDIS) {
                    // clear REDIS cache when update failed
                    var key = toCachePlayerDataKey(token.uid());
                    logger.debug("[redis:global] DEL {}", key);
                    globalRedisAsync().del(key);
                }
            }
            // throws concurrently update exception
            throw ConcurrentlyUpdateException.getInstance();
        }
    }

    public void fixPlayerBeforeUpdate(ServiceContext ctx) {
        // TODO do nothing now
    }

    public boolean updateCas(ServiceContext ctx) {
        fixPlayerBeforeUpdate(ctx);
        return updateCas(ctx.token(), ctx.player());
    }

    public boolean updateCas(AuthToken token, Player player) {
        if (player.updated()) {
            player.setUpdateTime(LocalDateTime.now());
            var filter = and(eq(player.getUid()), eq("_uv", player.getUpdateVersion()));
            player.increaseUpdateVersion();
            var updates = player.toUpdates();
            var update = Updates.combine(updates);
            var result = updateOnePlayer(token.gid(), filter, update);
            return result.getModifiedCount() > 0;
        }
        return true;
    }

    private UpdateResult updateOnePlayer(int groupId, Bson filter, Bson update) {
        logger.debug("[mongodb:player] update one {} ==> {}", filter, update);
        try {
            var result = playerBsonCollection(groupId).updateOne(filter, update);
            logger.debug("[mongodb:player] update result {} <<< {}", filter, result);
            return result;
        } catch (Exception e) {
            logger.error("[mongodb:player] Update one player failed: {} {}", filter, update, e);
            throw ApiErrors.dataAccessError(e);
        }
    }

    public boolean fixPlayerBeforeProcessing(ServiceContext ctx) {
        var player = ctx.player();
        var changed = false;
        // TODO may fix data for old versions
        // ...
        // check cross day
        var date = ctx.time().toLocalDate();
        var daily = player.getDaily();
        if (!daily.getDay().isEqual(date)) {
            changed = true;
            // cross day
            ctx.event(CROSS_DAY);
            // fix login
            var login = player.getLogin();
            login.increaseDays();
            if (daily.getDay().until(date, ChronoUnit.DAYS) == 1) {
                var cdays = login.increaseContinuousDays();
                if (cdays >= login.getMaxContinuousDays()) {
                    login.setMaxContinuousDays(cdays);
                }
                if (daily.getGamingCount() == 0) {
                    login.setGamingDays(0);
                }
            } else {
                login.setContinuousDays(1);
                login.setGamingDays(0);
            }
            // fix daily values
            daily.setDay(date);
            // 重置其他每日数据
            daily.setCoin(0);
            daily.setDiamond(0);
            daily.setVideoCount(0);
            daily.getVideoCounts().clear();
            daily.setGamingCount(0);
        }
        return changed;
    }

    public boolean fixPlayerAndUpdate(ServiceContext ctx) {
        if (fixPlayerBeforeProcessing(ctx)) {
            update(ctx);
            businessLogManager.logItemsAsync(ctx.itemLogs());
            return true;
        }
        return false;
    }

    public RedisFuture<String> cacheInRedisAsync(Player player) {
        var key = toCachePlayerDataKey(player.getUid());
        var value = toJsonData(player);
        return cacheInRedisAsync(key, value);
    }

    private RedisFuture<String> cacheInRedisAsync(String key, String value) {
        logger.debug("[redis:global] SETEX {} {} {}", key, CACHE_TTL_STR, value);
        return globalRedisAsync().setex(key, CACHE_TTL, value);
    }

    public void increaseVideoCount(ServiceContext ctx) {
        var player = ctx.player();
        player.getStatistics().increaseVideoCount();
        player.getDaily().increaseVideoCount();
    }

}
