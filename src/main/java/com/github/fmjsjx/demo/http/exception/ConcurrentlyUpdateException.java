package com.github.fmjsjx.demo.http.exception;

public class ConcurrentlyUpdateException extends DemoHttpException {

    private static final long serialVersionUID = -803416516807011744L;

    private static final ConcurrentlyUpdateException INSTANCE = new ConcurrentlyUpdateException();

    public static final ConcurrentlyUpdateException getInstance() {
        return INSTANCE;
    }

}
