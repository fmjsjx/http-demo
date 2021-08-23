package com.github.fmjsjx.demo.http.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TradeCashOut {

    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;

    public static final int SCORE_BONUS = 1;
    public static final int STAGE_BONUS = 2;
    public static final int DAILY_QUEST = 3;

    private int id;
    private int uid;
    private int type;
    private String tradeNo;
    private int amount;
    private int status;
    private String token;
    private String params;
    private String mchId;
    private String orderNo;
    private String response;
    private String error;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @JsonIgnore
    private transient Throwable cause;

    public TradeCashOut cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

}
