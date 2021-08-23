package com.github.fmjsjx.demo.http.sdk.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TransferResponse extends MchResponse {

    @JsonProperty("partner_trade_no")
    private String partnerTradeNo;
    @JsonProperty("payment_no")
    private String paymentNo;
    @JsonProperty("payment_time")
    private String paymentTime;

}
