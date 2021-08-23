package com.github.fmjsjx.demo.http.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.fmjsjx.demo.http.api.ApiResult;
import com.github.fmjsjx.demo.http.api.ResultData;
import com.github.fmjsjx.demo.http.api.video.AdvertParams;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.service.PlayerManager;
import com.github.fmjsjx.demo.http.service.VideoManager;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpGet;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPath;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPost;
import com.github.fmjsjx.libnetty.http.server.annotation.JsonBody;
import com.github.fmjsjx.libnetty.http.server.annotation.PropertyValue;
import com.github.fmjsjx.myboot.http.route.annotation.RouteController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@HttpPath("/api/players/@self")
@RouteController
public class SelfController {

    @Autowired
    private VideoManager videoManager;
    @Autowired
    private PlayerManager playerManager;

    @HttpGet
    @JsonBody
    public ApiResult get(@PropertyValue AuthToken token) {
        return token.lock(() -> {
            log.debug("[api:player] GET player: {}", token);
            var data = playerManager.autoRetry(retryCount -> {
                return get0(token, retryCount);
            });
            log.debug("[api:player] GET player result: {}", data);
            return ApiResult.ok(data);
        });
    }

    ResultData get0(AuthToken token, int retryCount) {
        var player = playerManager.getPlayer(token);
        var events = new ArrayList<String>();
        playerManager.fixPlayerAndUpdate(token, player, LocalDate.now(), events);
        return ResultData.create().sync(player).force().events(events);
    }

    @HttpPost("/arcodes")
    @JsonBody
    public ApiResult postArcodes(@PropertyValue AuthToken token, @JsonBody AdvertParams params) {
        return token.lock(() -> postArcodes0(token, params));
    }

    ApiResult postArcodes0(AuthToken token, AdvertParams params) {
        log.debug("[api:player] POST arcodes: {}", token);
        var arcode = videoManager.nextArcode(token, params.getAdvertId());
        log.debug("[api:player] Next arcode: {}", arcode);
        var data = ResultData.create(Map.of("arcode", arcode));
        log.debug("[api:player] POST arcodes result: {}", data);
        return ApiResult.ok(data);
    }

}
