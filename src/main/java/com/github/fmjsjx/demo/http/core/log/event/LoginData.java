package com.github.fmjsjx.demo.http.core.log.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.core.model.AuthToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoginData extends EventData<LoginData> {

    public static final LoginData create(AuthToken token) {
        var account = token.getAccount();
        return new LoginData(account.getType(), account.getPartner(), account.getGuestId(), account.getOpenid(),
                account.getUnionid(), account.getAppleId(), token.getIp(), token.getImei(), token.getOaid(),
                token.getDeviceInfo(), token.getOsInfo());
    }

    private int type;
    private int partner;
    @JsonProperty("guest_id")
    private String guestId;
    private String openid;
    private String unionid;
    @JsonProperty("apple_id")
    private String appleId;
    private String ip;
    private String imei;
    private String oaid;
    private String device;
    private String os;

}
