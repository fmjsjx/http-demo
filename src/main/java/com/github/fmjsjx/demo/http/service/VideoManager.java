package com.github.fmjsjx.demo.http.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.libcommon.redis.LuaScripts;
import com.github.fmjsjx.libcommon.redis.RedisUtil;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.StringUtil;

import io.lettuce.core.RedisFuture;

@Service
public class VideoManager extends RedisWrappedManager {

    private static final int ARCODE_TTL = 60 * 60;
    private static final String ARCODE_TTL_STR = String.valueOf(ARCODE_TTL);

    public static final String LOAD_VIDEO_FAILED = "load_video_failed";

    private static final String toArcodeKey(int uid) {
        return "player:{" + uid + "}:arcode";
    }

    private static final String codeValue(long uid, long unixTime) {
        var code = (uid << 32) | unixTime;
        return Long.toString(code, 36);
    }

    private static final String generateArcode(int advertId, int uid, long unixTime) {
        var codeValue = codeValue(uid, unixTime);
        return Integer.toHexString(advertId) + "." + codeValue;
    }
    
    private static final int parseAdvertId(String arcode) {
        var splited = arcode.split("\\.", 2);
        if (splited.length == 1) {
            return 0;
        }
        return Integer.parseUnsignedInt(splited[0], 16);
    }

    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private ConfigManager configManager;

    public String nextArcode(AuthToken token, int advertId) {
        var uid = token.uid();
        playerManager.lock(uid, "arcode", 1);
        var unixTime = DateTimeUtil.unixTime();
        var arcode = generateArcode(advertId, uid, unixTime);
        saveArcode(uid, arcode);
        return arcode;
    }

    public RedisFuture<String> saveArcode(int uid, String arcode) {
        var key = toArcodeKey(uid);
        logger.debug("[redis:global] SETEX {} {} {}", key, ARCODE_TTL_STR, arcode);
        return globalRedisAsync().setex(key, ARCODE_TTL, arcode);
    }

    public boolean ensureArcode(AuthToken token, String arcode, boolean valid) {
        if (!valid && StringUtil.isEmpty(arcode)) {
            throw ApiErrors.invalidArcode();
        }
        var uid = token.uid();
        var key = toArcodeKey(uid);
        var value = globalRedisSync().get(key);
        return validateValue(token, key, arcode, value);
    }

    private boolean validateValue(AuthToken token, String key, String arcode, String value) {
        if (value == null) {
            throw ApiErrors.invalidArcode();
        }
        if (LOAD_VIDEO_FAILED.equals(arcode)) {
            deleteIfPresent(key, value);
            return false;
        }
        if (!value.equals(arcode)) {
            throw ApiErrors.invalidArcode();
        }
        var advertId = parseAdvertId(arcode);
        if (configManager.advertShard(token).skipped(advertId)) {
            return false;
        }
        return true;
    }

    public boolean ensureArcode(AuthToken token, String arcode) {
        return ensureArcode(token, arcode, false);
    }

    public CompletionStage<Boolean> writeOffArcodeAsync(int uid, String arcode) {
        if (StringUtil.isEmpty(arcode) || LOAD_VIDEO_FAILED.equals(arcode)) {
            return CompletableFuture.completedStage(Boolean.FALSE);
        }
        var key = toArcodeKey(uid);
        return deleteIfPresent(key, arcode);
    }

    public CompletionStage<Boolean> deleteIfPresent(String key, String value) {
        String[] keys = { key };
        var script = LuaScripts.DEL_IF_VALUE_EQUALS;
        logger.debug("[redis:global] EVAL {} {} {}", script, keys, value);
        return RedisUtil.eval(globalRedisAsync(), script, keys, value);
    }

    public int toViewAd(boolean view, boolean loaded) {
        if (view) {
            return loaded ? 1 : -1;
        }
        return 0;
    }

}
