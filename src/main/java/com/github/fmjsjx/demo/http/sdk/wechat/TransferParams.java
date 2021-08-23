package com.github.fmjsjx.demo.http.sdk.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.fmjsjx.libcommon.util.DigestUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonRootName("xml")
public class TransferParams {

    public static final String NO_CHECK = "NO_CHECK";

    @JsonProperty("mch_appid")
    private String mchAppid;
    @JsonProperty("mchid")
    private String mchid;
    @JsonProperty("nonce_str")
    private String nonceStr;
    @JsonProperty("partner_trade_no")
    private String partnerTradeNo;
    @JsonProperty("openid")
    private String openid;
    @JsonProperty("check_name")
    private String checkName = NO_CHECK;
    @JsonProperty("amount")
    private int amount;
    @JsonProperty("desc")
    private String desc;
    @JsonProperty("spbill_create_ip")
    private String spbillCreateIp;

    @JsonProperty("sign")
    private String sign;

    public TransferParams signMd5(String key) {
        var baseStr = "amount=" + amount + "&check_name=" + checkName + "&desc=" + desc + "&mch_appid=" + mchAppid
                + "&mchid=" + mchid + "&nonce_str=" + nonceStr + "&openid=" + openid + "&partner_trade_no="
                + partnerTradeNo + "&spbill_create_ip=" + spbillCreateIp + "&key=" + key;
        this.sign = DigestUtil.md5AsHex(baseStr).toUpperCase();
        return this;
    }

}
