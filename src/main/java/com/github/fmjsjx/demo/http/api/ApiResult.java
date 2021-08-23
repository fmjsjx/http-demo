package com.github.fmjsjx.demo.http.api;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class ApiResult {

    public static final ApiResult ok() {
        return ok(ResultData.create());
    }

    public static final ApiResult ok(ResultData data) {
        return new ApiResult(ResultCodes.OK, null, data);
    }

    public static final ApiResult failed(int code, String message) {
        return new ApiResult(code, message, null);
    }

    private final int code;
    private final String message;
    private final ResultData data;

    public ApiResult(int code, String message, ResultData data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

}
