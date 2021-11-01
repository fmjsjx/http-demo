package com.github.fmjsjx.demo.http.controller;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.api.ApiResult;
import com.github.fmjsjx.demo.http.api.ResultData;
import com.github.fmjsjx.demo.http.api.video.AdvertParams;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.service.PlayerManager;
import com.github.fmjsjx.demo.http.service.VideoManager;
import com.github.fmjsjx.libnetty.http.server.annotation.ComponentValue;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpGet;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPath;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPost;
import com.github.fmjsjx.libnetty.http.server.annotation.JsonBody;
import com.github.fmjsjx.libnetty.http.server.annotation.PropertyValue;
import com.github.fmjsjx.libnetty.http.server.component.WorkerPool;
import com.github.fmjsjx.myboot.http.route.annotation.RouteController;

import io.netty.channel.EventLoop;
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
    public CompletionStage<ApiResult> get(@PropertyValue AuthToken token, @ComponentValue WorkerPool workerPool,
            EventLoop eventLoop) {
        return playerManager.lockAsync(token.uid(), 5, 30, eventLoop).thenApplyAsync(lock -> {
            log.debug("[api:player] GET player: {}", token);
            return lock.supplyThenUnlock(() -> {
                var data = playerManager.autoRetry(retryCount -> {
                    return get0(token, retryCount);
                });
                log.debug("[api:player] GET player result: {}", data);
                return ApiResult.ok(data);
            });
        }, workerPool.executor());
    }

    ResultData get0(AuthToken token, int retryCount) {
        var player = playerManager.getPlayer(token);
        var ctx = token.newContext(player);
        playerManager.fixPlayerAndUpdate(ctx);
        return ResultData.create().sync(player).force().events(ctx);
    }

    @HttpPost("/arcodes")
    @JsonBody
    public CompletionStage<ApiResult> postArcodes(@PropertyValue AuthToken token, @JsonBody AdvertParams params,
            @ComponentValue WorkerPool workerPool, EventLoop eventLoop) {
        return playerManager.tryLockAsync(token.uid(), "arcode", 1, eventLoop).thenApply(lock -> {
            log.debug("[api:player] POST arcodes: {}", token);
            return lock.orElseThrow(ApiErrors::clickTooQuick).supplyThenUnlock(() -> {
                var data = postArcodes0(token, params);
                log.debug("[api:player] POST arcodes result: {}", data);
                return ApiResult.ok(data);
            });
        });
    }

    ResultData postArcodes0(AuthToken token, AdvertParams params) {
        var arcode = videoManager.nextArcode(token, params.getAdvertId());
        log.debug("[api:player] Next arcode: {}", arcode);
        return ResultData.create(Map.of("arcode", arcode));
    }

}
