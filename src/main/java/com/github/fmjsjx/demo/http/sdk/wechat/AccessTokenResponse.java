package com.github.fmjsjx.demo.http.sdk.wechat;

import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.annotation.JsonWrapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AccessTokenResponse extends SnsResponse {

    private String accessToken;
    private Integer expiresIn;
    private String refreshToken;
    private String openid;
    private String scope;
    private String unionid;

    @JsonWrapper
    private void fillAliasesFields(@JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Integer expiresIn, @JsonProperty("refresh_token") String refreshToken) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
    }

}
