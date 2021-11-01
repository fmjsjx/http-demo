package com.github.fmjsjx.demo.http.service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.core.config.ServerConfig;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.libcommon.redis.LuaScripts;
import com.github.fmjsjx.libcommon.redis.RedisUtil;
import com.github.fmjsjx.libcommon.util.ArrayUtil;
import com.github.fmjsjx.libcommon.util.ChecksumUtil;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.StringUtil;

import io.lettuce.core.RedisFuture;

@Service
public class VideoManager extends RedisWrappedManager {

    private static final int ARCODE_TTL = 60 * 60;
    private static final String ARCODE_TTL_STR = String.valueOf(ARCODE_TTL);

    public static final String LOAD_VIDEO_FAILED = "load_video_failed";

    private static final String toClientArcodesKey(LocalDate date) {
        return "day:{" + DateTimeUtil.toNumber(date) + "}:client_arcodes";
    }

    private static final String toArcodeKey(int uid) {
        return "player:{" + uid + "}:arcode";
    }

    private static final int parseAdvertId(String arcode) {
        var docIndex = arcode.indexOf('.');
        if (docIndex > 0) {
            return Integer.parseUnsignedInt(arcode.substring(0, docIndex), 36);
        }
        return 0;
    }

    private static final String generateArcode(AuthToken token, int advertId, long unixTime, String secret) {
        var baseStr = Integer.toString(advertId, 36) + "." + Integer.toHexString(token.uid())
                + Long.toHexString(unixTime);
        var sign = Long.toString(ChecksumUtil.crc32((baseStr + "&" + token.id() + "&" + secret).getBytes()), 36);
        return baseStr + "." + sign;
    }

    @Autowired
    private ConfigManager configManager;

    public String nextArcode(AuthToken token, int advertId) {
        var unixTime = DateTimeUtil.unixTime();
        var arcode = generateArcode(token, advertId, unixTime, ServerConfig.advertConfig().clientArcodeSecret());
        saveArcode(token.uid(), arcode);
        return arcode;
    }

    public RedisFuture<String> saveArcode(int uid, String arcode) {
        var key = toArcodeKey(uid);
        logger.debug("[redis:global] SETEX {} {} {}", key, ARCODE_TTL_STR, arcode);
        return globalRedisAsync().setex(key, ARCODE_TTL, arcode);
    }

    public boolean ensureArcode(AuthToken token, String arcode, boolean valid) {
        if (token.clientArcode()) {
            return ensureClientArcode(token, arcode, valid);
        }
        if (!valid && StringUtil.isBlank(arcode)) {
            throw ApiErrors.invalidArcode();
        }
        var uid = token.uid();
        var key = toArcodeKey(uid);
        var value = globalRedisSync().get(key);
        return validateValue(token, key, arcode, value);
    }

    private boolean ensureClientArcode(AuthToken token, String arcode, boolean valid) {
        if (!valid && StringUtil.isBlank(arcode)) {
            throw ApiErrors.invalidArcode();
        }
        if (LOAD_VIDEO_FAILED.equals(arcode)) {
            return false;
        }
        var lastDot = arcode.lastIndexOf('.');
        if (lastDot == -1) {
            throw ApiErrors.invalidArcode();
        }
        var baseStr = arcode.substring(0, lastDot);
        var sign = arcode.substring(lastDot + 1);
        var secret = ServerConfig.advertConfig().clientArcodeSecret();
        var crc32 = ChecksumUtil.crc32((baseStr + "&" + token.id() + "&" + secret).getBytes());
        var expected = Long.toString(crc32, 36);
        if (!expected.equals(sign)) {
            throw ApiErrors.invalidArcode();
        }
        var docIndex = baseStr.indexOf('.');
        if (docIndex <= 0) {
            throw ApiErrors.invalidArcode();
        }
        var code = Long.parseLong(baseStr.substring(docIndex + 1), 16);
        var uid = code >>> 32;
        if (uid != token.uid()) {
            throw ApiErrors.invalidArcode();
        }
        var unixTime = code & 0xFFFFFFFFL;
        var key = toClientArcodesKey(DateTimeUtil.local(unixTime).toLocalDate());
        if (globalRedisSync().sismember(key, arcode)) {
            throw ApiErrors.invalidArcode();
        }
        var advertId = Integer.parseInt(baseStr.substring(0, docIndex), 36);
        return !configManager.advertShard(token).skipped(advertId);
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

    public CompletionStage<Boolean> writeOffArcodeAsync(AuthToken token, String arcode) {
        if (token.clientArcode()) {
            return writeOffClientArcodeAsync(arcode);
        }
        return writeOffArcodeAsync(token.uid(), arcode);
    }

    private CompletionStage<Boolean> writeOffClientArcodeAsync(String arcode) {
        if (StringUtil.isBlank(arcode) || LOAD_VIDEO_FAILED.equals(arcode)) {
            return CompletableFuture.completedStage(Boolean.FALSE);
        }
        var script = SADDEX_SCRIPT;
        var dotIndex = arcode.lastIndexOf('.');
        var unixTime = Long.parseLong(arcode.substring(dotIndex - 8, dotIndex), 16) & 0xFFFFFFFFL;
        var keys = ArrayUtil.self(toClientArcodesKey(DateTimeUtil.local(unixTime).toLocalDate()));
        var values = ArrayUtil.self(arcode, "86400");
        logger.debug("[redis:global] EVAL {} {} {}", script, keys, values);
        return RedisUtil.eval(globalRedisAsync(), script, keys, values);
    }

    private CompletionStage<Boolean> writeOffArcodeAsync(int uid, String arcode) {
        if (StringUtil.isBlank(arcode) || LOAD_VIDEO_FAILED.equals(arcode)) {
            return CompletableFuture.completedStage(Boolean.FALSE);
        }
        var key = toArcodeKey(uid);
        return deleteIfPresent(key, arcode);
    }

    private CompletionStage<Boolean> deleteIfPresent(String key, String value) {
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
