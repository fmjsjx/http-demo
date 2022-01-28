package com.github.fmjsjx.demo.http.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.fmjsjx.demo.http.WeChatProperties;
import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.api.ApiResult;
import com.github.fmjsjx.demo.http.api.ResultData;
import com.github.fmjsjx.demo.http.api.auth.LoginParams;
import com.github.fmjsjx.demo.http.api.auth.LoginResult;
import com.github.fmjsjx.demo.http.core.config.ServerConfig;
import com.github.fmjsjx.demo.http.core.config.ServerConfig.CacheMode;
import com.github.fmjsjx.demo.http.core.log.EventLog;
import com.github.fmjsjx.demo.http.core.log.Events.Auth;
import com.github.fmjsjx.demo.http.core.log.event.AccountData;
import com.github.fmjsjx.demo.http.core.log.event.LoginData;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.entity.Account;
import com.github.fmjsjx.demo.http.entity.model.Player;
import com.github.fmjsjx.demo.http.sdk.wechat.WeChatSdkClient;
import com.github.fmjsjx.demo.http.service.AccountManager;
import com.github.fmjsjx.demo.http.service.BusinessLogManager;
import com.github.fmjsjx.demo.http.service.ConfigManager;
import com.github.fmjsjx.demo.http.service.PlayerManager;
import com.github.fmjsjx.demo.http.service.TokenManager;
import com.github.fmjsjx.demo.http.util.ConfigUtil;
import com.github.fmjsjx.libcommon.collection.ArrayListSet;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.StringUtil;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPath;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPost;
import com.github.fmjsjx.libnetty.http.server.annotation.JsonBody;
import com.github.fmjsjx.libnetty.http.server.annotation.PathVar;
import com.github.fmjsjx.libnetty.http.server.annotation.RemoteAddr;
import com.github.fmjsjx.libnetty.http.server.exception.SimpleHttpFailureException;
import com.github.fmjsjx.myboot.http.route.annotation.RouteController;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RouteController
@HttpPath("/api/auth")
public class AuthController {

    private static final SimpleHttpFailureException unsupportedLoginType(String loginType) {
        return new SimpleHttpFailureException(HttpResponseStatus.NOT_FOUND,
                "Unsupported login type `" + loginType + "`");
    }

    private static final EventLog accountLog(LoginParams params, Account account) {
        var eventLog = new EventLog();
        eventLog.setProductId(params.getProductId());
        eventLog.setUid(account.getUid());
        eventLog.setChannel(params.getChannel());
        eventLog.setChannelId(params.getChannelId());
        eventLog.setClientVersion(params.getVersion());
        eventLog.setDeviceId(params.getDeviceId());
        eventLog.setSlot(account.getSlot());
        eventLog.setAudit(params.getAudit());
        eventLog.setEvent(Auth.ACCOUNT);
        eventLog.setData(AccountData.create(account));
        return eventLog;
    }

    @Autowired
    private AccountManager accountManager;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private BusinessLogManager businessLogManager;
    @Autowired
    private WeChatSdkClient weChatSdkClient;
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private ConfigManager configManager;

    @HttpPost("/{loginType}/login")
    @JsonBody
    public ApiResult postLogin(@PathVar("loginType") String loginType, @JsonBody LoginParams params,
            @RemoteAddr String ip) {
        switch (loginType) {
        case "guest":
            return postGuestLogin(params, ip);
        case "wechat":
            return postWeChatLogin(params, ip);
        default:
            throw unsupportedLoginType(loginType);
        }

    }

    ApiResult postGuestLogin(LoginParams params, String ip) {
        log.debug("[api:auth] Guest Login: {} {}", params, ip);
        var account = accountManager.getGuestAccount(params, ip);
        if (account.getState() == Account.FORBIDDEN) {
            throw ApiErrors.accountForbidden();
        }
        if (account.getRegister() == 1) {
            businessLogManager.logEventAsync(accountLog(params, account));
        }
        var now = LocalDateTime.now();
        var token = tokenManager.createToken(account, params, ip, now);
        var lock = playerManager.lock(account.getUid(), 15, 60_000, ApiErrors::dataAccessError);
        return lock.supplyThenUnlock(() -> {
            var attributes = params.getAttributes();
            var player = account.getRegister() == 1 ? playerManager.createGuestPlayer(token, attributes)
                    : playerManager.getGuestPlayer(token, attributes);
            player = onPlayerLoggedIn(token, player, null, null);
            checkForCache(token, player);
            var result = loginResult(token, player);
            result.setGuestId(account.getGuestId());
            var data = ResultData.create(result).sync(player).force();
            log.debug("[api:auth] Guest Login result: {}", data);
            return ApiResult.ok(data);
        });
    }

    private void checkForCache(AuthToken token, Player player) {
        var system = ServerConfig.getInstance().system();
        if (system.usePlayerCache()) {
            var cacheMode = system.playerCacheMode();
            if (cacheMode == CacheMode.LOCAL) {
                token.setProperty(Player.class, player);
            } else if (cacheMode == CacheMode.REDIS) {
                playerManager.cacheInRedisAsync(player);
            }
        }
    }

    private LoginResult loginResult(AuthToken token, Player player) {
        var result = new LoginResult();
        result.setUid(token.uid());
        result.setToken(token.id());
        result.setRegister(token.getAccount().getRegister());
        result.setRegisterTime(DateTimeUtil.toEpochSecond(token.getAccount().getCreateTime()));
        result.setSlot(token.getSlot());
        result.setConfig(configManager.clientShard(token).config());
        return result;
    }

    ApiResult postWeChatLogin(LoginParams params, String ip) {
        log.debug("[api:auth] WeChat Login: {} {}", params, ip);
        if (StringUtil.isNotBlank(params.getCode())) {
            return weChatLoginByCode(params, ip);
        }
        if (StringUtil.isBlank(params.getOpenid())) {
            throw new IllegalArgumentException("Missing Required Parameter `code`");
        }
        return weChatLoginByOpenId(params, ip);
    }

    private ApiResult weChatLoginByCode(LoginParams params, String ip) {
        var weChatApp = weChatProperties.getGlobal();
        var accessTokenResponse = weChatSdkClient.getAccessToken(weChatApp, params.getCode());
        var accLock = accountManager.lock(accessTokenResponse.getOpenid(), 30, 60_000);
        var account = accLock.supplyThenUnlock(() -> {
            var acc = accountManager.getWeChatAccount(params, ip, accessTokenResponse);
            if (acc.getState() == Account.FORBIDDEN) {
                throw ApiErrors.accountForbidden();
            }
            if (acc.getRegister() == 1) {
                businessLogManager.logEventAsync(accountLog(params, acc));
            }
            return acc;
        });
        var now = LocalDateTime.now();
        var token = tokenManager.createToken(account, params, ip, now);
        var user = weChatSdkClient.getUserInfoAsync(accessTokenResponse);
        var lock = playerManager.lock(account.getUid(), 15, 60_000, ApiErrors::dataAccessError);
        return lock.supplyThenUnlock(() -> {
            var attributes = params.getAttributes();
            var player = account.getRegister() == 1 ? playerManager.createPlayer(token, user, attributes)
                    : playerManager.getPlayer(token, user, attributes);
            player = onPlayerLoggedIn(token, player, user.getNickname(), user.getHeadimgurl());
            checkForCache(token, player);
            var result = loginResult(token, player);
            result.setOpenid(account.getOpenid());
            var data = ResultData.create(result).sync(player).force();
            log.debug("[api:auth] WeChat Login result: {}", data);
            return ApiResult.ok(data);
        });
    }

    private Player onPlayerLoggedIn(AuthToken token, Player player, String nickname, String faceUrl) {
        for (int i = 0; i <= ConfigUtil.retryCount(); i++) {
            var time = token.getLoginTime();
            var ctx = token.newContext(player, time);
            playerManager.fixPlayerBeforeProcessing(ctx);
            // fix basic info
            var basic = player.getBasic();
            if (StringUtil.isNotBlank(nickname)) {
                basic.setNickname(nickname);
            }
            if (StringUtil.isNotBlank(faceUrl)) {
                basic.setFaceUrl(faceUrl);
            }
            // fix features
            var preferences = player.getPreferences();
            var featuresShard = configManager.featuresShard(token);
            var newCommonFeatures = featuresShard.commonFeatures().stream().filter(token::hasFeature)
                    .filter(preferences::excludeFeature).filter(featuresShard::allowAll).toList();
            if (!newCommonFeatures.isEmpty()) {
                var features = new ArrayListSet<String>(preferences.getFeatures().size() + newCommonFeatures.size());
                features.internalList().addAll(preferences.getFeatures().internalList());
                features.internalList().addAll(newCommonFeatures);
                preferences.setFeatures(features);
            }
            // fix login info
            var login = player.getLogin();
            if (login.getLoginTime().isBefore(time)) {
                login.increaseCount();
                login.setLoginTime(time);
            }
            login.setIp(token.getIp());
            if (playerManager.updateCas(ctx)) {
                businessLogManager.logEventAsync(token, Auth.LOGIN, LoginData.create(token, player));
                return player;
            }
            player = playerManager.findPlayer(token.gid(), token.uid()).orElseThrow(ApiErrors::dataAccessError);
        }
        log.warn("[api:auth] Retry 3 times failed when update player on logged in !");
        throw ApiErrors.dataAccessError();
    }

    private ApiResult weChatLoginByOpenId(LoginParams params, String ip) {
        var accLock = accountManager.lock(params.getOpenid(), 30, 60_000);
        var account = accLock.supplyThenUnlock(() -> {
            var acc = accountManager.getWeChatAccount(params, ip).orElseThrow(ApiErrors::noSuchAccount);
            if (acc.getState() == Account.FORBIDDEN) {
                throw ApiErrors.accountForbidden();
            }
            return acc;
        });
        var now = LocalDateTime.now();
        var token = tokenManager.createToken(account, params, ip, now);
        var lock = playerManager.lock(account.getUid(), 15, 60_000L, ApiErrors::dataAccessError);
        return lock.supplyThenUnlock(() -> {
            var player = playerManager.findPlayer(token.gid(), token.uid()).orElseThrow(ApiErrors::requireWechatCode);
            player = onPlayerLoggedIn(token, player, null, null);
            checkForCache(token, player);
            var result = loginResult(token, player);
            result.setOpenid(account.getOpenid());
            var data = ResultData.create(result).sync(player).force();
            log.debug("[api:auth] WeChat Login result: {}", data);
            return ApiResult.ok(data);
        });
    }

}
