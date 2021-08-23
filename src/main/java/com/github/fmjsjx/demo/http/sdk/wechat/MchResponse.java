package com.github.fmjsjx.demo.http.sdk.wechat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonRootName("xml")
public class MchResponse {

    public static final String SYSTEMERROR = "SYSTEMERROR";
    public static final String FREQ_LIMIT = "FREQ_LIMIT";

    @JsonIgnore
    private String bodyContent;

    @JsonProperty("return_code")
    private String returnCode;
    @JsonProperty("return_msg")
    private String returnMsg;
    @JsonProperty("mch_appid")
    private String mchAppid;
    @JsonProperty("mchid")
    private String mchid;
    @JsonProperty("device_info")
    private String deviceInfo;
    @JsonProperty("nonce_str")
    private String nonceStr;
    @JsonProperty("result_code")
    private String resultCode;
    @JsonProperty("err_code")
    private String errCode;
    @JsonProperty("err_code_des")
    private String errCodeDes;

    public boolean returnSuccess() {
        return "SUCCESS".equals(returnCode);
    }

    public boolean resultSuccess() {
        return "SUCCESS".equals(resultCode);
    }

}
