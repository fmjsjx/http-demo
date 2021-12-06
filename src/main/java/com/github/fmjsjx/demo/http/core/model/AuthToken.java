package com.github.fmjsjx.demo.http.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import com.github.fmjsjx.demo.http.api.Constants.ClientFeatures;
import com.github.fmjsjx.demo.http.core.log.EventLog;
import com.github.fmjsjx.demo.http.core.log.ItemLog;
import com.github.fmjsjx.demo.http.entity.Account;
import com.github.fmjsjx.demo.http.entity.model.Player;

public interface AuthToken {

    Class<AuthToken> KEY = AuthToken.class;

    String id();

    default int uid() {
        return getAccount().getUid();
    }

    default int gid() {
        return getAccount().getGid();
    }

    Account getAccount();

    int getProductId();

    String getChannel();

    int getChannelId();

    String getIp();

    String getClientVersion();

    String getDeviceId();

    int getSlot();

    int getAudit();

    default boolean audit() {
        return getAudit() == 1;
    }

    String getImei();

    String getOaid();

    String getDeviceInfo();

    String getOsInfo();

    LocalDateTime getLoginTime();

    Set<String> getFeatures();

    default boolean hasFeature(String feature) {
        return getFeatures().contains(feature);
    }

    default boolean clientArcode() {
        return getFeatures().contains(ClientFeatures.CLIENT_ARCODE);
    }

    default int toRegisterDays(LocalDate today) {
        return (int) toRegisterDate().until(today, ChronoUnit.DAYS) + 1;
    }

    default LocalDate toRegisterDate() {
        return getAccount().getCreateTime().toLocalDate();
    }

    default EventLog eventLog(String event, Object data) {
        return new EventLog(this, event, data);
    }

    default ItemLog itemLog(int itemId, long original, long number, int sourceId, String remark) {
        return new ItemLog(this, itemId, original, number, sourceId, remark);
    }

    default boolean hasProperty(Object key) {
        return property(key).isPresent();
    }

    <T> Optional<T> property(Object key);

    <T> Optional<T> property(Object key, Class<T> type);

    AuthToken setProperty(Object key, Object value);

    Object putProperty(Object key, Object value);

    <T> Optional<T> removeProperty(Object key);

    boolean removeProperty(Object key, Object value);

    default ServiceContext newContext() {
        return ServiceContext.create(this);
    }

    default ServiceContext newContext(LocalDateTime time) {
        return ServiceContext.create(this, time);
    }

    default ServiceContext newContext(Player player) {
        return ServiceContext.create(this, player);
    }

    default ServiceContext newContext(Player player, LocalDateTime time) {
        return ServiceContext.create(this, player, time);
    }

}
