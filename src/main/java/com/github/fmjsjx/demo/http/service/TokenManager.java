package com.github.fmjsjx.demo.http.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.github.fmjsjx.demo.http.api.auth.LoginParams;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.entity.Account;
import com.github.fmjsjx.demo.http.util.DeviceUtil;
import com.github.fmjsjx.libcommon.json.Jackson2Library;
import com.github.fmjsjx.libcommon.json.JsoniterLibrary;
import com.github.fmjsjx.libcommon.util.ChecksumUtil;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.NumberUtil;
import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.any.Any;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenManager extends RedisWrappedManager implements InitializingBean {

    public static final int X_TOKEN_ALIVE_SECONDS = 86400;
    private static final String X_TOKEN_TTL = String.valueOf(X_TOKEN_ALIVE_SECONDS);

    private static final long CONFUSION = 0x4b5a3c69;

    private static final byte[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    /**
     * Generate token by the specified {@code uid} and {@code unixTime}.
     * 
     * @param uid      the User ID
     * @param unixTime the number of seconds from the epoch of
     *                 {@code 1970-01-01T00:00:00Z}.
     * @return the generated token
     */
    public static final String generateToken(int uid, long unixTime) {
        var b = new byte[24];
        toHexBytes(unixTime, b, 0);
        var u = uid ^ unixTime ^ CONFUSION;
        toHexBytes(u, b, 8);
        var checkCode = checkCode(uid, unixTime);
        toHexBytes(checkCode, b, 16);
        var token = new String(b, StandardCharsets.US_ASCII);
        if (log.isTraceEnabled()) {
            log.trace("Generated token: {} <== uid={}, unixTime={}, checkCode={}", token, uid, unixTime, checkCode);
        }
        return token;
    }

    private static final long checkCode(int uid, long unixTime) {
        return ChecksumUtil.crc32c(longToBytes(((unixTime << 32) | uid)));
    }

    private static final void toHexBytes(long value, byte[] dest, int offset) {
        dest[offset] = digits[(int) ((value >>> 28) & 0xf)];
        dest[offset + 1] = digits[(int) ((value >>> 24) & 0xf)];
        dest[offset + 2] = digits[(int) ((value >>> 20) & 0xf)];
        dest[offset + 3] = digits[(int) ((value >>> 16) & 0xf)];
        dest[offset + 4] = digits[(int) ((value >>> 12) & 0xf)];
        dest[offset + 5] = digits[(int) ((value >>> 8) & 0xf)];
        dest[offset + 6] = digits[(int) ((value >>> 4) & 0xf)];
        dest[offset + 7] = digits[(int) (value & 0xf)];
    }

    private static final byte[] longToBytes(long value) {
        var b = new byte[8];
        b[0] = (byte) (value >>> 56);
        b[1] = (byte) (value >>> 48);
        b[2] = (byte) (value >>> 40);
        b[3] = (byte) (value >>> 32);
        b[4] = (byte) (value >>> 24);
        b[5] = (byte) (value >>> 16);
        b[6] = (byte) (value >>> 8);
        b[7] = (byte) (value >>> 0);
        return b;
    }

    /**
     * Returns {@code true} if the specified token is self valid, {@code false}
     * otherwise.
     * 
     * @param token the token
     * @return {@code true} if the specified token is self valid, {@code false}
     *         otherwise
     */
    public static final boolean isTokenSelfValid(String token) {
        if (token.length() != 24) {
            // length error
            return false;
        }
        byte[] b = token.getBytes();
        if (b.length != 24) {
            // length error
            return false;
        }
        try {
            var unixTime = parseHex(b, 0, 8);
            var u = parseHex(b, 8, 8);
            var checkCode = parseHex(b, 16, 8);
            var uid = (int) (u ^ unixTime ^ CONFUSION);
            return checkCode == checkCode(uid, unixTime);
        } catch (Throwable e) {
            // failure if any error occurs
            return false;
        }
    }

    private static final long parseHex(byte[] bytes, int offset, int length) {
        var v = 0L;
        for (int i = offset; i < offset + length; i++) {
            var b = bytes[i];
            int c;
            if (b >= '0' && b <= '9') {
                c = b - '0';
            } else if (b >= 'a' && b <= 'f') {
                c = b - 'a' + 10;
            } else {
                throw new NumberFormatException();
            }
            v = (v << 4) | c;
        }
        return v;
    }

    private static final String toTokenKey(String tokenId) {
        return "auth:token:{" + tokenId + "}";
    }

    private static final String toPreTokenKey(int uid) {
        return "auth:player:{" + uid + "}:pre-token";
    }

    private static final ConcurrentHashMap<String, AuthTokenImpl> createCachedMap() {
        return new ConcurrentHashMap<>(65536);
    }

    @Autowired
    @Qualifier("globalScheduledExecutor")
    private ScheduledExecutorService globalScheduledExecutor;

    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    private volatile ConcurrentMap<String, AuthTokenImpl> cachedTokens0 = createCachedMap();
    private volatile ConcurrentMap<String, AuthTokenImpl> cachedTokens1 = createCachedMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Start cache obsolescence task.");
        globalScheduledExecutor.scheduleAtFixedRate(() -> {
            logger.debug("[cache:obsolescence] eliminate once...");
            try {
                var lock = cacheLock.writeLock();
                lock.lock();
                try {
                    var obsolescent = cachedTokens0;
                    logger.debug("[cache:obsolescence] obsolescent ==> {}", obsolescent);
                    cachedTokens0 = cachedTokens1;
                    cachedTokens1 = createCachedMap();
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                logger.error("Unexpected error occurs when run obsolescence task", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    public AuthToken createToken(Account account, LoginParams params, String ip, LocalDateTime time) {
        var token = new AuthTokenImpl(account, params, ip, time);
        var preTokenKey = toPreTokenKey(token.uid());
        globalRedisAsync().get(preTokenKey).thenAccept(preTokenId -> {
            if (preTokenId != null) {
                removeCachedToken(preTokenId);
                var key = toTokenKey(preTokenId);
                log.debug("[redis:global] DEL {}", key);
                globalRedisAsync().del(key);
            }
        });
        var tokenId = token.id();
        var key = toTokenKey(tokenId);
        var value = Jackson2Library.getInstance().dumps(token);
        log.debug("[redis:global] SETEX {} {} {}", key, X_TOKEN_ALIVE_SECONDS, value);
        globalRedisAsync().setex(key, X_TOKEN_ALIVE_SECONDS, value);
        log.debug("[redis:global] SETEX {} {} {}", preTokenKey, X_TOKEN_ALIVE_SECONDS, tokenId);
        globalRedisAsync().setex(preTokenKey, X_TOKEN_ALIVE_SECONDS, tokenId);
        cacheToken(token);
        return token;
    }

    private void removeCachedToken(String tokenId) {
        var lock = cacheLock.readLock();
        lock.lock();
        try {
            cachedTokens0.remove(tokenId);
            cachedTokens1.remove(tokenId);
        } finally {
            lock.unlock();
        }
    }

    private void cacheToken(AuthTokenImpl token) {
        cachedTokens1.put(token.id(), token);
    }

    private AuthTokenImpl findCachedToken(String tokenId) {
        var lock = cacheLock.readLock();
        lock.lock();
        try {
            var token = cachedTokens1.get(tokenId);
            if (token == null) {
                token = cachedTokens0.get(tokenId);
                if (token != null) {
                    cachedTokens1.put(tokenId, token);
                }
            }
            return token;
        } finally {
            lock.unlock();
        }
    }

    public Optional<AuthToken> findToken(String id) {
        if (!isTokenSelfValid(id)) {
            return Optional.empty();
        }
        var cached = findCachedToken(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        var key = toTokenKey(id);
        var value = globalRedisSync().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            var token = JsoniterLibrary.getInstance().loads(value, AuthTokenImpl.class);
            log.debug("[redis:global] Hit token {} ==> {}", key, token);
            cacheToken(token);
            return Optional.of(token);
        } catch (Exception e) {
            log.warn("[redis:global] Parse Token falied: {} {}", key, value, e);
            globalRedisAsync().del(key);
            return Optional.empty();
        }
    }

    public CompletionStage<Optional<AuthToken>> findTokenAsync(String id, Executor executor) {
        return findTokenAsync(id, true, executor);
    }

    public CompletionStage<Optional<AuthToken>> findTokenAsync(String id, boolean delayExpired, Executor executor) {
        if (!isTokenSelfValid(id)) {
            return CompletableFuture.completedStage(Optional.empty());
        }
        var key = toTokenKey(id);
        var cached = findCachedToken(id);
        if (cached != null) {
            if (delayExpired) {
                // delay expired time
                log.debug("[redis:global] EXPIRE {} {}", key, X_TOKEN_TTL);
                globalRedisAsync().expire(key, X_TOKEN_ALIVE_SECONDS);
                var preTokenKey = toPreTokenKey(cached.uid());
                log.debug("[redis:global] SETEX {} {} {}", preTokenKey, X_TOKEN_ALIVE_SECONDS, id);
                globalRedisAsync().setex(preTokenKey, X_TOKEN_ALIVE_SECONDS, id);
            }
            return CompletableFuture.completedStage(Optional.of(cached));
        }
        return globalRedisAsync().get(key).thenApplyAsync(value -> {
            if (value == null) {
                return Optional.empty();
            }
            try {
                var token = JsoniterLibrary.getInstance().loads(value, AuthTokenImpl.class);
                log.debug("[redis:global] Hit token {} ==> {}", key, token);
                cacheToken(token);
                if (delayExpired) {
                    // delay expired time
                    log.debug("[redis:global] EXPIRE {} {}", key, X_TOKEN_TTL);
                    globalRedisAsync().expire(key, X_TOKEN_ALIVE_SECONDS);
                    var preTokenKey = toPreTokenKey(token.uid());
                    log.debug("[redis:global] SETEX {} {} {}", preTokenKey, X_TOKEN_ALIVE_SECONDS, id);
                    globalRedisAsync().setex(preTokenKey, X_TOKEN_ALIVE_SECONDS, id);
                }
                return Optional.of(token);
            } catch (Exception e) {
                log.warn("[redis:global] Parse Token falied: {} {}", key, value, e);
                globalRedisAsync().del(key);
                return Optional.empty();
            }
        }, executor);
    }

    public AuthToken parseToken(Any any) {
        return any.as(AuthTokenImpl.class);
    }

    @ToString
    public static final class AuthTokenImpl implements AuthToken {

        private transient volatile String id;

        private final Account account;
        private final int productId;
        private final String channel;
        private final int channelId;
        private final String ip;
        private final String clientVersion;
        private final String deviceId;
        private final int slot;
        private final String imei;
        private final String oaid;
        private final String deviceInfo;
        private final String osInfo;
        @JsonFormat(shape = Shape.STRING)
        private final LocalDateTime loginTime;
        private final Set<String> features;

        private transient final ConcurrentMap<Object, Object> properties = new ConcurrentHashMap<>(4);

        @JsonCreator
        public AuthTokenImpl(Account account, int productId, String channel, Integer channelId, String ip,
                String clientVersion, String deviceId, int slot, String imei, String oaid, String deviceInfo,
                String osInfo, LocalDateTime loginTime, List<String> features) {
            this.account = account;
            this.productId = productId;
            this.channel = channel;
            this.channelId = NumberUtil.intValue(channelId, account.getChannelId());
            this.ip = ip;
            this.clientVersion = clientVersion;
            this.deviceId = deviceId;
            this.slot = slot;
            this.imei = imei;
            this.oaid = oaid;
            this.deviceInfo = deviceInfo;
            this.osInfo = osInfo;
            this.loginTime = loginTime;
            this.features = Set.copyOf(features.stream().map(String::intern).collect(Collectors.toList()));
        }

        AuthTokenImpl(Account account, LoginParams params, String ip, LocalDateTime loginTime) {
            this(account, params.getProductId(), params.getChannel(), params.getChannelId(), ip, params.getVersion(),
                    params.getDeviceId(), DeviceUtil.calculateSlot(params.getDeviceId()), params.getImei(),
                    params.getOaid(), params.getDeviceInfo(), params.getOsInfo(), loginTime, params.getFeatures());
        }

        @Override
        public String id() {
            var id = this.id;
            if (id == null) {
                synchronized (this) {
                    id = this.id;
                    if (id == null) {
                        this.id = id = generateToken(uid(), DateTimeUtil.toEpochSecond(loginTime));
                    }
                }
            }
            return id;
        }

        @Override
        public Account getAccount() {
            return account;
        }

        @Override
        public int getProductId() {
            return productId;
        }

        @Override
        public String getChannel() {
            return channel;
        }

        @Override
        public int getChannelId() {
            return channelId;
        }

        @Override
        public String getIp() {
            return ip;
        }

        @Override
        public String getClientVersion() {
            return clientVersion;
        }

        @Override
        public String getDeviceId() {
            return deviceId;
        }

        @Override
        public int getSlot() {
            return slot;
        }

        @Override
        public String getImei() {
            return imei;
        }

        @Override
        public String getOaid() {
            return oaid;
        }

        @Override
        public String getDeviceInfo() {
            return deviceInfo;
        }

        @Override
        public String getOsInfo() {
            return osInfo;
        }

        @Override
        public LocalDateTime getLoginTime() {
            return loginTime;
        }

        @Override
        public Set<String> getFeatures() {
            return features;
        }

        @Override
        public boolean hasProperty(Object key) {
            return properties.containsKey(key);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<T> property(Object key) {
            var value = properties.get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of((T) value);
        }

        @Override
        public <T> Optional<T> property(Object key, Class<T> type) {
            var value = properties.get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(type.cast(value));
        }

        @Override
        public AuthToken setProperty(Object key, Object value) {
            properties.put(key, value);
            return this;
        }

        @Override
        public Object putProperty(Object key, Object value) {
            return properties.put(key, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<T> removeProperty(Object key) {
            var value = properties.remove(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of((T) value);
        }

        @Override
        public boolean removeProperty(Object key, Object value) {
            return properties.remove(key, value);
        }

    }

}
