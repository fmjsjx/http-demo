package com.github.fmjsjx.demo.http.api;

public final class ResultCodes {

    public static final int OK = 0;

    public static final int UNKNOWN_ERROR = 1;
    public static final int INVALID_CONNECTION = 2;
    public static final int SERVER_CLOSED = 3;
    public static final int DUPLICATE_REQUEST = 4;

    public static final int NO_SUCH_METHOD = 100;
    public static final int INVALID_PARAMETER = 101;
    public static final int DATA_ACCESS_ERRROR = 102;
    public static final int MESSAGE_TIMEOUT = 103;

    public static final int CONCURRENTLY_LOGIN = 201;
    public static final int INVALID_TOKEN = 202;
    public static final int ACCOUNT_FORBIDDEN = 203;
    public static final int NO_SUCH_ACCOUNT = 204;
    public static final int PARTNER_AUTH_FAILURE = 211;
    public static final int REQUIRE_WECHAT_CODE = 212;

    public static final int CLICK_TOO_QUICK = 1001;
    public static final int INVALID_ARCODE = 1002;
    public static final int ALREADY_SIGNED = 1003;
    public static final int COUNT_LIMITED_TODAY = 1004;
    public static final int NO_SUCH_BONUS = 1005;
    public static final int NO_SUCH_QUEST = 1006;
    public static final int QUEST_NOT_COMPLETED = 1007;
    public static final int ALREADY_BONUS = 1008;
    public static final int ALREADY_LOGIN = 1009;

    public static final int NO_ENOUGH_RESOURCE = 1100;
    public static final int NO_ENOUGH_COIN = 1101;
    public static final int NO_ENOUGH_DIAMOND = 1102;
    public static final int NO_ENOUGH_ROPE = 1103;
    public static final int NO_ENOUGH_ITEM = 1110;

    public static final int CATTLE_ROPING_ALREADY_END = 1200;

    public static final int PARTNER_SERVICE_ERROR = 1301;

    public static final int CASH_OUT_ERROR = 1401;
    public static final int CASH_OUT_LIMITED = 1402;
    public static final int CASH_OUT_GAMING_DAYS = 1403;
    public static final int CASH_OUT_DISABLED = 1404;

    public static final int CASH_OUT_GUEST_DISABLED = 1410;
    public static final int CASH_OUT_PLATFORM_DISABLED = 1411;

    public static final boolean isError(int code) {
        return code != OK;
    }

    public static final boolean isMajorError(int code) {
        return isError(code) && code < 1000;
    }

    private ResultCodes() {

    }

}
