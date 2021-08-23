package com.github.fmjsjx.demo.http.api;

import com.github.fmjsjx.demo.http.exception.ApiErrorException;
import com.github.fmjsjx.demo.http.exception.ParameterizedErrorException;

public class ApiErrors {

    public static final ApiErrorException dataAccessError(Throwable cause) {
        return new ApiErrorException(ResultCodes.DATA_ACCESS_ERRROR, "Data Access Error", cause);
    }

    public static final ApiErrorException dataAccessError() {
        return new ApiErrorException(ResultCodes.DATA_ACCESS_ERRROR, "Data Access Error");
    }

    public static final ApiErrorException accountForbidden() {
        return new ApiErrorException(ResultCodes.ACCOUNT_FORBIDDEN, "Account Forbidden");
    }

    public static final ApiErrorException invalidToken() {
        return new ApiErrorException(ResultCodes.INVALID_TOKEN, "Invalid Token");
    }

    public static final ApiErrorException noSuchAccount() {
        return new ApiErrorException(ResultCodes.NO_SUCH_ACCOUNT, "No Such Account");
    }

    public static final ApiErrorException partnerAuthFailure() {
        return new ApiErrorException(ResultCodes.PARTNER_AUTH_FAILURE, "Partner Auth Failure");
    }

    public static final ApiErrorException partnerAuthFailure(Throwable cause) {
        return new ApiErrorException(ResultCodes.PARTNER_AUTH_FAILURE, "Partner Auth Failure", cause);
    }

    public static final ApiErrorException partnerServiceError() {
        return new ApiErrorException(ResultCodes.PARTNER_SERVICE_ERROR, "Partner Service Error");
    }

    public static final ApiErrorException partnerServiceError(Throwable cause) {
        return new ApiErrorException(ResultCodes.PARTNER_SERVICE_ERROR, "Partner Service Error", cause);
    }

    public static final ApiErrorException requireWechatCode() {
        return new ApiErrorException(ResultCodes.REQUIRE_WECHAT_CODE, "Require WeChat Code");
    }

    public static final ApiErrorException clickTooQuick() {
        return new ApiErrorException(ResultCodes.CLICK_TOO_QUICK, "Click Too Quick");
    }

    public static final ApiErrorException invalidArcode() {
        return new ApiErrorException(ResultCodes.INVALID_ARCODE, "Invalid Arcode");
    }

    public static final ApiErrorException countLimitedToday() {
        return new ApiErrorException(ResultCodes.COUNT_LIMITED_TODAY, "Count Limited Today");
    }

    public static final ApiErrorException noSuchBonus() {
        return new ApiErrorException(ResultCodes.NO_SUCH_BONUS, "No Such Bonus");
    }

    public static final ApiErrorException alreadyBonus() {
        return new ApiErrorException(ResultCodes.ALREADY_BONUS, "Already Bonus");
    }

    public static final ApiErrorException questNotCompleted() {
        return new ApiErrorException(ResultCodes.QUEST_NOT_COMPLETED, "Quest Not Completed");
    }

    public static final ApiErrorException cattleRopingAlreadyEnd() {
        return new ApiErrorException(ResultCodes.CATTLE_ROPING_ALREADY_END, "Cattle Roping Already End");
    }

    public static final ApiErrorException noEnoughResource() {
        return new ApiErrorException(ResultCodes.NO_ENOUGH_RESOURCE, "No Enough Resource");
    }

    public static final ApiErrorException noEnoughCoin() {
        return new ApiErrorException(ResultCodes.NO_ENOUGH_COIN, "No Enough Coin");
    }

    public static final ApiErrorException noEnoughDiamond() {
        return new ApiErrorException(ResultCodes.NO_ENOUGH_DIAMOND, "No Enough Diamond");
    }

    public static final ApiErrorException noEnoughRope() {
        return new ApiErrorException(ResultCodes.NO_ENOUGH_ROPE, "No Enough Rope");
    }

    public static final ApiErrorException noEnoughItem() {
        return new ApiErrorException(ResultCodes.NO_ENOUGH_ITEM, "No Enough Item");
    }

    public static final ApiErrorException cashOutError(String message) {
        return new ParameterizedErrorException(ResultCodes.CASH_OUT_ERROR, "Cash Out Failed: {0}", message);
    }

    public static final ApiErrorException cashOutError(Throwable cause, String message) {
        return new ParameterizedErrorException(ResultCodes.CASH_OUT_ERROR, "Cash Out Failed: {0}", cause, message);
    }

    public static final ApiErrorException cashOutLimited() {
        return new ApiErrorException(ResultCodes.CASH_OUT_LIMITED, "Cash Out Limited");
    }

    public static final ApiErrorException cashOutGaming60() {
        return cashOutGamingDays(60);
    }

    public static final ApiErrorException cashOutDisabled() {
        return new ApiErrorException(ResultCodes.CASH_OUT_DISABLED, "今日份额已满，请明日再提");
    }

    public static final ApiErrorException cashOutGamingDays(int days) {
        return new ParameterizedErrorException(ResultCodes.CASH_OUT_GAMING_DAYS, "连续玩游戏{0}天即可提现", days);
    }

    public static final ApiErrorException cashOutGuestDisabled() {
        return new ApiErrorException(ResultCodes.CASH_OUT_GUEST_DISABLED, "Cash Out Guest Disabled");
    }

    public static final ApiErrorException cashOutPlatformDisabled() {
        return new ApiErrorException(ResultCodes.CASH_OUT_PLATFORM_DISABLED, "Cash Out Platform Disabled");
    }

}
