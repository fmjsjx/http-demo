package com.github.fmjsjx.demo.http.api.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginResult {

    private int uid;
    private String token;
    private String guestId;
    private String openid;
    private String appleId;
    private int register;

    private long registerTime;
    private int slot;
    private String wlxPlatform;

}
