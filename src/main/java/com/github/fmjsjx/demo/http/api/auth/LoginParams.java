package com.github.fmjsjx.demo.http.api.auth;

import java.util.List;
import java.util.Map;

import com.github.fmjsjx.libcommon.util.StringUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginParams {

    // QTT
    private String ticket;
    private String platform;
    
    // WeChat
    private String code;
    private String openid;
    
    // Apple ID
    private String identityToken;
    private String appleId;

    private int productId;
    private String channel;
    private int channelId;
    private String version;
    private String deviceId;
    private String imei;
    private String oaid;
    private int audit;
    private String deviceInfo;
    private String osInfo;
    private List<String> features = List.of();
    private Map<String, String> attributes = Map.of();

    public void validateRequired() {
        if (StringUtil.isEmpty(channel)) {
            throw new IllegalArgumentException("`channel` is required");
        }
        if (StringUtil.isEmpty(version)) {
            throw new IllegalArgumentException("`version` is required");
        }
        if (StringUtil.isEmpty(deviceId)) {
            throw new IllegalArgumentException("`deviceId` is required");
        }
    }

}
