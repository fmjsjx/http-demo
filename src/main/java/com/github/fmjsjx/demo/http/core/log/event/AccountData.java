package com.github.fmjsjx.demo.http.core.log.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.entity.Account;

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
public class AccountData extends EventData<AccountData> {

    public static final AccountData create(Account account) {
        return new AccountData(account.getType(), account.getPartner(), account.getGuestId(), account.getOpenid(),
                account.getUnionid(), account.getAppleId(), account.getIp(), account.getImei(), account.getOaid(),
                account.getDeviceInfo(), account.getOsInfo(), account.getExtS1());
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
    @JsonProperty("wlx_platform")
    private String wlxPlatform;

}
