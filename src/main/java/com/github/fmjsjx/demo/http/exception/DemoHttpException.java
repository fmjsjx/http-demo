package com.github.fmjsjx.demo.http.exception;

import java.util.concurrent.CompletionException;

public abstract class DemoHttpException extends CompletionException {

    private static final long serialVersionUID = 1108993665031022125L;

    public DemoHttpException() {
    }

    public DemoHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemoHttpException(String message) {
        super(message);
    }

    public DemoHttpException(Throwable cause) {
        super(cause);
    }

}
