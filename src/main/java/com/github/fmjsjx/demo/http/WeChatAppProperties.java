package com.github.fmjsjx.demo.http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WeChatAppProperties {

    private String appid;
    private String secret;

    private String mchid;
    private String mchSecret;

}
