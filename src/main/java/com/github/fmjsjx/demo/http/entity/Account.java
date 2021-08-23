package com.github.fmjsjx.demo.http.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Account {

    /**
     * 游客账号 {@code 1}
     */
    public static final int GUEST = 1;
    /**
     * 第三方平台账号 {@code 2}
     */
    public static final int PARTNER = 2;

    /**
     * 正常 {@code 1}
     */
    public static final int NORMAL = 1;
    /**
     * 禁用 {@code 2}
     */
    public static final int FORBIDDEN = 2;

    private int uid;
    private int gid;
    private int type;
    private int state;
    private int productId;
    private String channel;
    private int channelId;
    private int partner;
    private String guestId;
    private String openid;
    private String unionid;
    private String appleId;
    private String ip;
    private String clientVersion;
    private String deviceId;
    private int slot;
    private String deviceInfo;
    private String osInfo;
    private String imei;
    private String oaid;
    private String extS1; // used ext_s1 to store wlx_platform
    @JsonFormat(shape = Shape.STRING)
    private LocalDateTime createTime;
    @JsonFormat(shape = Shape.STRING)
    private LocalDateTime updateTime;

    // only when this account is just created, then this value will be 1
    @JsonIgnore
    private transient int register;

    @JsonIgnore
    public boolean isGuest() {
        return type == GUEST;
    }

}
