package com.github.fmjsjx.demo.http.sdk.wechat;

import java.util.List;

import com.github.fmjsjx.demo.http.sdk.PartnerUserInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class UserInfoResponse extends SnsResponse implements PartnerUserInfo {

    private String openid;
    private String nickname;
    private int sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private List<String> privilege;
    private String unionid;

    @Override
    public String nickname() {
        return nickname;
    }
    
    @Override
    public String faceUrl() {
        return headimgurl;
    }
    
}
