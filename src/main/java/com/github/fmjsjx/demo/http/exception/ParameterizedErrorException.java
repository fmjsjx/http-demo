package com.github.fmjsjx.demo.http.exception;

import com.github.fmjsjx.demo.http.core.config.ErrorMessageConfig;
import com.github.fmjsjx.libcommon.util.ArrayUtil;

public class ParameterizedErrorException extends ApiErrorException {

    private static final long serialVersionUID = 2980857591585620451L;

    private final Object[] params;

    public ParameterizedErrorException(int code, String message, Throwable cause, Object... params) {
        super(code, message, cause);
        this.params = params;
    }

    public ParameterizedErrorException(int code, String message, Object... params) {
        super(code, message);
        this.params = params;
    }

    public ParameterizedErrorException(int code, Throwable cause, Object... params) {
        super(code, cause);
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + " : " + ArrayUtil.toString(params);
    }

    public String message() {
        return ErrorMessageConfig.getFactory(getCode()).message(getMessage(), params);
    }

}
