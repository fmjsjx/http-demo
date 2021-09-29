package com.github.fmjsjx.demo.http.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.fmjsjx.demo.http.api.ApiResult;
import com.github.fmjsjx.demo.http.api.ResultData;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.service.PlayerManager;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpGet;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPath;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPut;
import com.github.fmjsjx.libnetty.http.server.annotation.JsonBody;
import com.github.fmjsjx.libnetty.http.server.annotation.PropertyValue;
import com.github.fmjsjx.libnetty.http.server.annotation.StringBody;
import com.github.fmjsjx.myboot.http.route.annotation.RouteController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@HttpPath("/api/players/@self/preferences")
@RouteController
public class PreferencesController {

    @Autowired
    private PlayerManager playerManager;

    @HttpGet("/custom")
    @JsonBody
    public ApiResult getCustom(@PropertyValue AuthToken token) {
        return token.lock(() -> {
            log.debug("[api:player.references] GET custom: {}", token);
            var data = playerManager.autoRetry(retryCount -> {
                return getCustom0(token, retryCount);
            });
            log.debug("[api:player.references] GET custom result: {}", data);
            return ApiResult.ok(data);
        });
    }

    private ResultData getCustom0(AuthToken token, int retryCount) {
        var ctx = token.newContext();
        var player = playerManager.getPlayer(ctx);
        var custom = player.getPreferences().getCustom();
        playerManager.fixPlayerAndUpdate(ctx);
        return ctx.toResultData(custom, retryCount);
    }

    @HttpPut("/custom")
    @JsonBody
    public ApiResult putCustom(@PropertyValue AuthToken token, @StringBody String custom) {
        return token.lock(() -> {
            log.debug("[api:player.references] PUT custom: {} - {}", custom, token);
            var data = playerManager.autoRetry(retryCount -> {
                return putCustom0(token, custom, retryCount);
            });
            log.debug("[api:player.references] PUT custom result: {}", data);
            return ApiResult.ok(data);
        });
    }

    ResultData putCustom0(AuthToken token, String custom, int retryCount) {
        var ctx = token.newContext();
        var player = playerManager.getPlayer(ctx);
        player.getPreferences().setCustom(custom);
        playerManager.update(ctx);
        return ctx.toResultData(retryCount);
    }
}
