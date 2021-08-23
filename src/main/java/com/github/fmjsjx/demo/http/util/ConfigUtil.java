package com.github.fmjsjx.demo.http.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.netty.util.internal.SystemPropertyUtil;

public class ConfigUtil {

    private static final class ConfDirHolder {
        private static final String CONF_DIR = System.getProperty("conf.dir", "conf");
    }

    public static final String confDir() {
        return ConfDirHolder.CONF_DIR;
    }

    public static final File confFile(String filename) {
        return new File(confDir(), filename);
    }

    public static final InputStream openConfFile(String filename) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(confFile(filename)));
    }

    private static final class RetryCountHolder {
        private static final int RETRY_COUNT = SystemPropertyUtil.getInt("api.retryCount", 3);
    }

    public static final int retryCount() {
        return RetryCountHolder.RETRY_COUNT;
    }

    private static final int COIN_LIMIT = SystemPropertyUtil.getInt("player.coinLimit", 30000);

    public static final int coinLimit() {
        return COIN_LIMIT;
    }

    private ConfigUtil() {
    }

}
