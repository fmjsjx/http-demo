package com.github.fmjsjx.demo.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ConfigurationProperties("wechat")
public class WeChatProperties {

    private WeChatAppProperties global;

}
