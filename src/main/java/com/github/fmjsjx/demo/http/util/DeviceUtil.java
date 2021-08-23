package com.github.fmjsjx.demo.http.util;

public class DeviceUtil {

    public static final int calculateSlot(String deviceId) {
        return String.valueOf(deviceId).hashCode() & 0xF;
    }

}
