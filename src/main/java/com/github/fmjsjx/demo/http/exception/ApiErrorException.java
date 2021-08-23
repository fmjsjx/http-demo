package com.github.fmjsjx.demo.http.exception;

import com.github.fmjsjx.demo.http.api.ApiResult;
import com.github.fmjsjx.demo.http.core.config.ErrorMessageConfig;

public class ApiErrorException extends DemoHttpException {

    private static final long serialVersionUID = 893625340552785539L;

    private final int code;

    public ApiErrorException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ApiErrorException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ApiErrorException(int code, Throwable cause) {
        this(code, cause.getMessage(), cause);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getLocalizedMessage() {
        var cause = getCause();
        return (cause != null) ? (getMessage() + " - " + cause.toString()) : getMessage();
    }

    public String message() {
        return ErrorMessageConfig.getFactory(code).message(getMessage());
    }

    public ApiResult toResult() {
        return ApiResult.failed(code, message());
    }

}
