package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.libcommon.util.RandomUtil;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;

@ToString
public class ServerConfig {

    private static final ServerConfig DEFAULT_INSTANCE = new ServerConfig();

    private static final AtomicReference<ServerConfig> INSTANCE_REF = new AtomicReference<>(DEFAULT_INSTANCE);

    public static final ServerConfig getInstance() {
        return INSTANCE_REF.get();
    }

    public static final SystemConfig systemConfig() {
        return getInstance().system();
    }

    public static final CashConfig cashConfig() {
        return getInstance().cash();
    }

    public static final ServerConfig set(ServerConfig config) {
        return INSTANCE_REF.getAndSet(config);
    }

    public static final void resetToDefault() {
        INSTANCE_REF.set(DEFAULT_INSTANCE);
    }

    public static final ServerConfig loadFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ServerConfig.class);
    }

    final SystemConfig system;
    final CashConfig cash;

    @JsonCreator
    public ServerConfig(@JsonProperty(value = "system", required = false) SystemConfig system,
            @JsonProperty(value = "cash", required = false) CashConfig cash) {
        this.system = Optional.ofNullable(system).orElseGet(SystemConfig::new);
        this.cash = Optional.ofNullable(cash).orElseGet(CashConfig::new);
    }

    ServerConfig() {
        this(null, null);
    }

    public SystemConfig system() {
        return system;
    }

    public CashConfig cash() {
        return cash;
    }

    @ToString
    public static final class SystemConfig {

        final boolean useAccountCache;
        final boolean usePlayerCache;
        final CacheMode playerCacheMode;
        final boolean playerSyncForcedly;

        @JsonCreator
        public SystemConfig(@JsonProperty(value = "use-account-cache", required = false) Boolean useAccountCache,
                @JsonProperty(value = "use-player-cache", required = false) boolean usePlayerCache,
                @JsonProperty(value = "player-cache-mode", required = false) String playerCacheMode,
                @JsonProperty(value = "player-sync-forcedly", required = false) boolean playerSyncForcedly) {
            this(useAccountCache == null ? true : useAccountCache, usePlayerCache,
                    playerCacheMode == null ? CacheMode.NONE : CacheMode.valueOf(playerCacheMode.toUpperCase()),
                    playerSyncForcedly);
        }

        SystemConfig(boolean useAccountCache, boolean usePlayerCache, CacheMode playerCacheMode,
                boolean playerSyncForcedly) {
            this.useAccountCache = useAccountCache;
            this.usePlayerCache = usePlayerCache;
            this.playerCacheMode = playerCacheMode;
            this.playerSyncForcedly = playerSyncForcedly;
        }

        public SystemConfig() {
            this(true, false, CacheMode.NONE, false);
        }

        public boolean useAccountCache() {
            return useAccountCache;
        }

        public boolean usePlayerCache() {
            return usePlayerCache;
        }

        public CacheMode playerCacheMode() {
            return playerCacheMode;
        }

        public boolean playerSyncForcedly() {
            return playerSyncForcedly;
        }

    }

    public enum CacheMode {

        LOCAL, REDIS, NONE

    }

    @ToString
    public static final class CashConfig {

        final boolean guestEnabled;
        final int guestSuccessRate;

        final boolean wechatEnabled;

        @JsonCreator
        public CashConfig(@JsonProperty(value = "guest-enabled", required = false) boolean enableGuest,
                @JsonProperty(value = "guest-success-rate", required = false) int guestSuccessRate,
                @JsonProperty(value = "wechat-disbaled", required = false) boolean wechatEnabled) {
            this.guestEnabled = enableGuest;
            this.guestSuccessRate = guestSuccessRate;
            this.wechatEnabled = wechatEnabled;
        }

        public CashConfig() {
            this(false, 0, false);
        }

        public boolean guestEnabled() {
            return guestEnabled;
        }

        public int guestSuccessRate() {
            return guestSuccessRate;
        }

        public boolean guestHitSuccess() {
            if (guestEnabled) {
                var rate = guestSuccessRate;
                if (rate >= 100) {
                    return true;
                }
                if (rate <= 0) {
                    return false;
                }
                return RandomUtil.randomInt(100) < rate;
            }
            return false;
        }

        public boolean wechatEnabled() {
            return wechatEnabled;
        }

    }

}
