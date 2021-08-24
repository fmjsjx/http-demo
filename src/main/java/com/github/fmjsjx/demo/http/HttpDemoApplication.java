package com.github.fmjsjx.demo.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.fmjsjx.demo.http.util.BannerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class HttpDemoApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(HttpDemoApplication.class, args);
        var app = ctx.getBean(AppProperties.class);
        BannerUtil.printGameBanner(s -> log.info("-- Banner --\n{}", s), app.getName(), app.getVersion());
    }

}
