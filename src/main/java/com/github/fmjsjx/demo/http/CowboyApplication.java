package com.github.fmjsjx.demo.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.fmjsjx.demo.http.util.BannerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class CowboyApplication {

    public static void main(String[] args) {
        var appClass = CowboyApplication.class;
        var ctx = SpringApplication.run(appClass, args);
        var app = ctx.getBean(AppProperties.class);
        BannerUtil.printGameBanner(s -> log.info("-- Banner --\n{}", s), app.getName(), app.getVersion());
    }

}
