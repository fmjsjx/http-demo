package com.github.fmjsjx.demo.http.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.api.Constants.Partners;
import com.github.fmjsjx.demo.http.api.auth.LoginParams;
import com.github.fmjsjx.demo.http.core.config.ServerConfig;
import com.github.fmjsjx.demo.http.dao.AccountMapper;
import com.github.fmjsjx.demo.http.entity.Account;
import com.github.fmjsjx.demo.http.sdk.wechat.AccessTokenResponse;
import com.github.fmjsjx.demo.http.util.DeviceUtil;
import com.github.fmjsjx.libcommon.json.Jackson2Library;
import com.github.fmjsjx.libcommon.json.JsoniterLibrary;
import com.github.fmjsjx.libcommon.util.StringUtil;

import io.lettuce.core.RedisException;
import io.lettuce.core.RedisFuture;

@Service
public class AccountManager extends RedisWrappedManager {

    private static final String KEY_GUEST_UID_MAPPINGS = "auth:guest:uid:mappings";
    private static final String KEY_WECHAT_UID_MAPPINGS = "auth:wechat:uid:mappings";

    private static final String toCachedAccountKey(int uid) {
        return "cache:account:{" + uid + "}";
    }

    private static final boolean useAccountCache() {
        return ServerConfig.getInstance().system().useAccountCache();
    }

    @Autowired
    private AccountMapper mapper;
    @Autowired
    private MongoDBManager mongoDBManager;

    private final int gid() {
        return mongoDBManager.randomGroupId();
    }

    public Account getGuestAccount(LoginParams params, String ip) {
        try {
            var guestId = params.getDeviceId();
            var ouid = findMappedGuestUid(guestId);
            if (ouid.isPresent()) {
                return findAccount(ouid.getAsInt()).orElseGet(() -> {
                    removeMappedGuestId(guestId);
                    return createGuestAccount(params, ip);
                });
            }
            return createGuestAccount(params, ip);
        } catch (DataAccessException | RedisException e) {
            throw ApiErrors.dataAccessError(e);
        }
    }

    public Account createGuestAccount(LoginParams params, String ip) {
        var account = createAccount(params, ip);
        account.setType(Account.GUEST);
        account.setState(Account.NORMAL);
        account.setPartner(Partners.NONE);
        account.setGuestId(params.getDeviceId());
        try {
            mapper.insertOne(account);
            writeGuestUidMappingAsync(account);
            var now = LocalDateTime.now();
            account.setCreateTime(now);
            account.setUpdateTime(now);
            account.setRegister(1);
        } catch (DuplicateKeyException e) {
            account = mapper.selectOneByGuestId(params.getDeviceId()).get();
            logger.debug("Reloaded guest account: {}", account);
        }
        writeGuestUidMappingAsync(account);
        return account;
    }

    private Account createAccount(LoginParams params, String ip) {
        var account = new Account();
        account.setProductId(params.getProductId());
        account.setGid(gid());
        account.setChannel(params.getChannel());
        account.setChannelId(params.getChannelId());
        account.setClientVersion(params.getVersion());
        account.setDeviceId(params.getDeviceId());
        account.setSlot(DeviceUtil.calculateSlot(params.getDeviceId()));
        if (StringUtil.isNotBlank(params.getImei())) {
            account.setImei(params.getImei());
        }
        if (StringUtil.isNotBlank(params.getOaid())) {
            account.setOaid(params.getOaid());
        }
        account.setDeviceInfo(params.getDeviceInfo());
        account.setOsInfo(params.getOsInfo());
        account.setIp(ip);
        return account;
    }

    private CompletionStage<Boolean> writeGuestUidMappingAsync(Account account) {
        var field = account.getGuestId();
        var value = Integer.toString(account.getUid());
        logger.debug("[redis:global] HSET {} {} {}", KEY_GUEST_UID_MAPPINGS, field, value);
        return globalRedisAsync().hset(KEY_GUEST_UID_MAPPINGS, field, value);
    }

    public OptionalInt findMappedGuestUid(String guestId) {
        var uidValue = globalRedisSync().hget(KEY_GUEST_UID_MAPPINGS, guestId);
        if (uidValue == null) {
            return OptionalInt.empty();
        }
        try {
            var uid = Integer.parseInt(uidValue);
            return OptionalInt.of(uid);
        } catch (Exception e) {
            logger.warn("[redis:global] Mapped Guest UID error: {} >>> {}", guestId, uidValue);
            removeMappedGuestId(guestId);
        }
        return OptionalInt.empty();
    }

    public Optional<Account> findAccount(int uid) {
        var key = toCachedAccountKey(uid);
        if (useAccountCache()) {
            var value = globalRedisSync().get(key);
            if (value != null) {
                try {
                    return Optional.of(JsoniterLibrary.getInstance().loads(value, Account.class));
                } catch (Exception e) {
                    logger.warn("[redis:global] Parse account from JSON falied: {} <<< {}", value, key, e);
                    globalRedisAsync().del(key);
                }
            }
        }
        var account = mapper.selectOne(uid);
        if (account.isPresent()) {
            cacheAccount(key, account.get());
        }
        return account;
    }

    private RedisFuture<String> cacheAccount(String key, Account account) {
        var value = Jackson2Library.getInstance().dumpsToString(account);
        logger.debug("[redis:global] SET {} {}", key, value);
        return globalRedisAsync().setex(key, 7 * 86400, value);
    }

    public RedisFuture<Long> removeMappedGuestId(String guestId) {
        logger.debug("[redis:global] HDEL {} {}", KEY_GUEST_UID_MAPPINGS, guestId);
        return globalRedisAsync().hdel(KEY_GUEST_UID_MAPPINGS, guestId);
    }

    public OptionalInt findMappedWeChatUid(String openid) {
        var uidValue = globalRedisSync().hget(KEY_WECHAT_UID_MAPPINGS, openid);
        if (uidValue == null) {
            return OptionalInt.empty();
        }
        try {
            var uid = Integer.parseInt(uidValue);
            return OptionalInt.of(uid);
        } catch (Exception e) {
            logger.warn("[redis:global] Mapped WeChat UID error: {} >>> {}", openid, uidValue);
            removeMappedWeChatOpenid(openid);
        }
        return OptionalInt.empty();
    }

    public RedisFuture<Long> removeMappedWeChatOpenid(String openid) {
        logger.debug("[redis:global] HDEL {} {}", KEY_WECHAT_UID_MAPPINGS, openid);
        return globalRedisAsync().hdel(KEY_WECHAT_UID_MAPPINGS, openid);
    }

    public Account getWeChatAccount(LoginParams params, String ip, AccessTokenResponse accessTokenResponse) {
        try {
            var ouid = findMappedWeChatUid(accessTokenResponse.getOpenid());
            if (ouid.isPresent()) {
                return findAccount(ouid.getAsInt()).orElseGet(() -> {
                    removeMappedWeChatOpenid(accessTokenResponse.getOpenid());
                    return createWeChatAccount(params, ip, accessTokenResponse);
                });
            }
            return createWeChatAccount(params, ip, accessTokenResponse);
        } catch (DataAccessException | RedisException e) {
            throw ApiErrors.dataAccessError(e);
        }
    }

    private Account createWeChatAccount(LoginParams params, String ip, AccessTokenResponse accessTokenResponse) {
        var account = createAccount(params, ip);
        account.setType(Account.PARTNER);
        account.setState(Account.NORMAL);
        account.setPartner(Partners.WECHAT);
        account.setOpenid(accessTokenResponse.getOpenid());
        account.setUnionid(accessTokenResponse.getUnionid());
        try {
            mapper.insertOne(account);
            writeWeChatUidMappingAsync(account);
            var now = LocalDateTime.now();
            account.setCreateTime(now);
            account.setUpdateTime(now);
            account.setRegister(1);
        } catch (DuplicateKeyException e) {
            account = mapper.selectOneByOpenid(Partners.WECHAT, accessTokenResponse.getOpenid()).get();
            logger.debug("Reloaded WeChat account: {}", account);
        }
        writeWeChatUidMappingAsync(account);
        return account;
    }

    private RedisFuture<Boolean> writeWeChatUidMappingAsync(Account account) {
        var field = account.getOpenid();
        var value = Integer.toString(account.getUid());
        logger.debug("[redis:global] HSET {} {} {}", KEY_WECHAT_UID_MAPPINGS, field, value);
        return globalRedisAsync().hset(KEY_WECHAT_UID_MAPPINGS, field, value);
    }

    public Optional<Account> getWeChatAccount(LoginParams params, String ip) {
        var ouid = findMappedWeChatUid(params.getOpenid());
        if (ouid.isPresent()) {
            var acc = findAccount(ouid.getAsInt());
            if (acc.isEmpty()) {
                removeMappedWeChatOpenid(params.getOpenid());
            }
            return acc;
        }
        return Optional.empty();

    }

}
