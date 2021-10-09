package com.github.fmjsjx.demo.http.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.api.ApiResult;
import com.github.fmjsjx.demo.http.api.ResultData;
import com.github.fmjsjx.demo.http.api.core.ArcodeParams;
import com.github.fmjsjx.demo.http.api.video.VideoBonusInfo;
import com.github.fmjsjx.demo.http.core.config.VideoBonusConfig.VideoConfig;
import com.github.fmjsjx.demo.http.core.log.Events.Videos;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.service.BusinessLogManager;
import com.github.fmjsjx.demo.http.service.ConfigManager;
import com.github.fmjsjx.demo.http.service.PlayerManager;
import com.github.fmjsjx.demo.http.service.VideoManager;
import com.github.fmjsjx.demo.http.util.ItemUtil;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpGet;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPath;
import com.github.fmjsjx.libnetty.http.server.annotation.HttpPost;
import com.github.fmjsjx.libnetty.http.server.annotation.JsonBody;
import com.github.fmjsjx.libnetty.http.server.annotation.PathVar;
import com.github.fmjsjx.libnetty.http.server.annotation.PropertyValue;
import com.github.fmjsjx.libnetty.http.server.exception.SimpleHttpFailureException;
import com.github.fmjsjx.myboot.http.route.annotation.RouteController;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@HttpPath("/api/videos/{videoId}")
@RouteController
public class VideosController {

    private static final SimpleHttpFailureException noSuchVideo(int videoId) {
        return new SimpleHttpFailureException(HttpResponseStatus.NOT_FOUND, "no such video with id `" + videoId + "`");
    }

    @Autowired
    private ConfigManager configManager;
    @Autowired
    private VideoManager videoManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private BusinessLogManager businessLogManager;

    @HttpGet
    @JsonBody
    public ApiResult get(@PropertyValue AuthToken token, @PathVar("videoId") int videoId) {
        return token.lock(() -> {
            var video = configManager.videoBonusShard(token).video(videoId).orElseThrow(() -> noSuchVideo(videoId));
            log.debug("[api:videos] GET video info: {} <== {}", video, token);
            if (video.dailyLimit().isEmpty() && video.limit().isEmpty()) {
                var result = new VideoBonusInfo(video.id(), -1).bonus(video.bonus());
                var data = ResultData.create(result);
                log.debug("[api:videos] GET video info result: {}", data);
                return ApiResult.ok(data);
            }
            return playerManager.lock(token.uid(), 5, 10_000).supplyThenUnlock(() -> {
                var data = playerManager.autoRetry(retryCount -> {
                    return get0(token, video, retryCount);
                });
                log.debug("[api:videos] GET video info result: {}", data);
                return ApiResult.ok(data);
            });
        });
    }

    ResultData get0(AuthToken token, VideoConfig video, int retryCount) {
        var player = playerManager.getPlayer(token);
        var ctx = token.newContext(player);
        playerManager.fixPlayerAndUpdate(ctx);
        var remaining = -1;
        var limit = video.limit();
        if (limit.isPresent()) {
            var count = player.getStatistics().getVideoCounts().get(video.id()).orElse(0);
            remaining = Math.max(0, limit.getAsInt() - count);
        }
        var dailyLimit = video.dailyLimit();
        if (dailyLimit.isPresent()) {
            var dailyVideoCounts = player.getDaily().getVideoCounts();
            var dailyCount = dailyVideoCounts.get(video.id()).orElse(0);
            var dr = Math.max(0, dailyLimit.getAsInt() - dailyCount);
            if (remaining >= 0) {
                remaining = Math.min(remaining, dr);
            } else {
                remaining = dr;
            }
        }
        var result = new VideoBonusInfo(video.id(), remaining).bonus(video.bonus());
        return ctx.toResultData(result, retryCount);
    }

    @HttpPost("/bonus")
    @JsonBody
    public ApiResult postBonus(@PropertyValue AuthToken token, @PathVar("videoId") int videoId,
            @JsonBody ArcodeParams params) {
        return token.lock(() -> {
            var video = configManager.videoBonusShard(token).video(videoId).orElseThrow(() -> noSuchVideo(videoId));
            log.debug("[api:videos] POST video bonus: {} {} <== {}", video, params, token);
            return playerManager.lock(token.uid(), 10, 30_000).supplyThenUnlock(() -> {
                var counting = videoManager.ensureArcode(token, params.getArcode());
                var data = playerManager.autoRetry(retryCount -> {
                    return postBonus0(token, params, video, counting, retryCount);
                });
                log.debug("[api:videos] GET video info result: {}", data);
                return ApiResult.ok(data);
            });
        });
    }

    ResultData postBonus0(AuthToken token, ArcodeParams params, VideoConfig video, boolean counting, int retryCount) {
        var videoId = video.id();
        var ctx = token.newContext();
        var player = playerManager.getPlayer(ctx);
        var statistics = player.getStatistics();
        var videoCounts = statistics.getVideoCounts();
        var count = videoCounts.get(videoId).orElse(0);
        var remaining = -1;
        var limit = video.limit();
        if (limit.isPresent()) {
            remaining = Math.max(0, limit.getAsInt() - count);
            if (remaining == 0) {
                throw ApiErrors.noSuchBonus();
            }
        }
        var daily = player.getDaily();
        var dailyCount = daily.getVideoCounts().get(videoId).orElse(0);
        var dailyLimit = video.dailyLimit();
        if (dailyLimit.isPresent()) {
            var dr = Math.max(0, dailyLimit.getAsInt() - dailyCount);
            if (dr == 0) {
                throw ApiErrors.countLimitedToday();
            }
            if (remaining >= 0) {
                remaining = Math.min(remaining, dr);
            } else {
                remaining = dr;
            }
        }
        videoCounts.put(videoId, count + 1);
        daily.getVideoCounts().put(videoId, dailyCount + 1);
        if (counting) {
            PlayerManager.increaseVideoCount(player);
        }
        var bonus = video.bonus();
        if (bonus.isEmpty()) {
            if (video.policy().isPresent()) {
                var policies = configManager.bonusPoliciesShard(token).policies(video.policy().get());
                bonus = List.of(policies.switchBonus(player.getWallet().getCoin()).toBox());
            }
        }
        var itemLogs = ItemUtil.addItems(token, player, bonus, video.sourceId(), video.remark());
        playerManager.update(ctx);
        videoManager.writeOffArcodeAsync(token.uid(), params.getArcode());
        if (remaining > 0) {
            remaining--;
        }
        businessLogManager.logEventAsync(token, Videos.bonus(video.name()),
                Map.of("video_id", video.id(), "count", count + 1, "remaining", remaining, "bonus", bonus));
        businessLogManager.logItemsAsync(itemLogs);
        var result = Map.of("id", video.id(), "remaining", remaining, "bonus", bonus);
        return ctx.toResultData(result, retryCount);
    }

}
