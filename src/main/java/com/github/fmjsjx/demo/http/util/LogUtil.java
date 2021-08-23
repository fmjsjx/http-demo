package com.github.fmjsjx.demo.http.util;

public class LogUtil {

    public static final String toUnixTime(long timeMillis) {
        var ms = timeMillis % 1000;
        if (ms >= 1000) {
            return (timeMillis / 1000) + "." + ms;
        } else if (ms >= 100) {
            return (timeMillis / 1000) + ".0" + ms;
        } else {
            return (timeMillis / 1000) + ".00" + ms;
        }
    }

    private LogUtil() {
    }
}
