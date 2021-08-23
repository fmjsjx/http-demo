package com.github.fmjsjx.demo.http.entity.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.bson.BsonUtil;
import com.github.fmjsjx.libcommon.bson.DotNotation;
import com.github.fmjsjx.libcommon.bson.model.ObjectModel;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class LoginInfo extends ObjectModel<LoginInfo> {

    private static final DotNotation XPATH = DotNotation.of("lgn");

    private final Player parent;

    private int count;
    private int days;
    private int continuousDays;
    private int maxContinuousDays;
    private int gamingDays;
    private int maxGamingDays;
    private String ip;
    @JsonIgnore
    private LocalDateTime loginTime;

    public LoginInfo(Player parent) {
        this.parent = parent;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (this.count != count) {
            this.count = count;
            updatedFields.set(1);
        }
    }

    public int increaseCount() {
        var count = this.count += 1;
        updatedFields.set(1);
        return count;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        if (this.days != days) {
            this.days = days;
            updatedFields.set(2);
        }
    }

    public int increaseDays() {
        var days = this.days += 1;
        updatedFields.set(2);
        return days;
    }

    public int getContinuousDays() {
        return continuousDays;
    }

    public void setContinuousDays(int continuousDays) {
        if (this.continuousDays != continuousDays) {
            this.continuousDays = continuousDays;
            updatedFields.set(3);
        }
    }

    public int increaseContinuousDays() {
        var continuousDays = this.continuousDays += 1;
        updatedFields.set(3);
        return continuousDays;
    }

    public int getMaxContinuousDays() {
        return maxContinuousDays;
    }

    public void setMaxContinuousDays(int maxContinuousDays) {
        if (this.maxContinuousDays != maxContinuousDays) {
            this.maxContinuousDays = maxContinuousDays;
            updatedFields.set(4);
        }
    }

    public int getGamingDays() {
        return gamingDays;
    }

    public void setGamingDays(int gamingDays) {
        if (this.gamingDays != gamingDays) {
            this.gamingDays = gamingDays;
            updatedFields.set(5);
        }
    }

    public int increaseGamingDays() {
        var gamingDays = this.gamingDays += 1;
        updatedFields.set(5);
        return gamingDays;
    }

    public int getMaxGamingDays() {
        return maxGamingDays;
    }

    public void setMaxGamingDays(int maxGamingDays) {
        if (this.maxGamingDays != maxGamingDays) {
            this.maxGamingDays = maxGamingDays;
            updatedFields.set(6);
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        if (ObjectUtil.isNotEquals(this.ip, ip)) {
            this.ip = ip;
            updatedFields.set(7);
        }
    }

    @JsonIgnore
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        if (ObjectUtil.isNotEquals(this.loginTime, loginTime)) {
            this.loginTime = loginTime;
            updatedFields.set(8);
        }
    }

    @Override
    public Player parent() {
        return parent;
    }

    @Override
    public DotNotation xpath() {
        return XPATH;
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("cnt", new BsonInt32(count));
        bson.append("d", new BsonInt32(days));
        bson.append("cnd", new BsonInt32(continuousDays));
        bson.append("mcd", new BsonInt32(maxContinuousDays));
        bson.append("gmd", new BsonInt32(gamingDays));
        bson.append("mgd", new BsonInt32(maxGamingDays));
        bson.append("ip", new BsonString(ip));
        if (loginTime != null) {
            bson.append("lgt", BsonUtil.toBsonDateTime(loginTime));
        }
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("cnt", count);
        doc.append("d", days);
        doc.append("cnd", continuousDays);
        doc.append("mcd", maxContinuousDays);
        doc.append("gmd", gamingDays);
        doc.append("mgd", maxGamingDays);
        doc.append("ip", ip);
        if (loginTime != null) {
            doc.append("lgt", DateTimeUtil.toLegacyDate(loginTime));
        }
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("cnt", count);
        data.put("d", days);
        data.put("cnd", continuousDays);
        data.put("mcd", maxContinuousDays);
        data.put("gmd", gamingDays);
        data.put("mgd", maxGamingDays);
        data.put("ip", ip);
        if (loginTime != null) {
            data.put("lgt", DateTimeUtil.toEpochMilli(loginTime));
        }
        return data;
    }

    @Override
    public void load(Document src) {
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        days = BsonUtil.intValue(src, "d").orElse(0);
        continuousDays = BsonUtil.intValue(src, "cnd").orElse(0);
        maxContinuousDays = BsonUtil.intValue(src, "mcd").orElse(0);
        gamingDays = BsonUtil.intValue(src, "gmd").orElse(0);
        maxGamingDays = BsonUtil.intValue(src, "mgd").orElse(0);
        ip = BsonUtil.stringValue(src, "ip").orElse("");
        loginTime = BsonUtil.dateTimeValue(src, "lgt").orElse(null);
    }

    @Override
    public void load(BsonDocument src) {
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        days = BsonUtil.intValue(src, "d").orElse(0);
        continuousDays = BsonUtil.intValue(src, "cnd").orElse(0);
        maxContinuousDays = BsonUtil.intValue(src, "mcd").orElse(0);
        gamingDays = BsonUtil.intValue(src, "gmd").orElse(0);
        maxGamingDays = BsonUtil.intValue(src, "mgd").orElse(0);
        ip = BsonUtil.stringValue(src, "ip").orElse("");
        loginTime = BsonUtil.dateTimeValue(src, "lgt").orElse(null);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        days = BsonUtil.intValue(src, "d").orElse(0);
        continuousDays = BsonUtil.intValue(src, "cnd").orElse(0);
        maxContinuousDays = BsonUtil.intValue(src, "mcd").orElse(0);
        gamingDays = BsonUtil.intValue(src, "gmd").orElse(0);
        maxGamingDays = BsonUtil.intValue(src, "mgd").orElse(0);
        ip = BsonUtil.stringValue(src, "ip").orElse("");
        loginTime = BsonUtil.dateTimeValue(src, "lgt").orElse(null);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        days = BsonUtil.intValue(src, "d").orElse(0);
        continuousDays = BsonUtil.intValue(src, "cnd").orElse(0);
        maxContinuousDays = BsonUtil.intValue(src, "mcd").orElse(0);
        gamingDays = BsonUtil.intValue(src, "gmd").orElse(0);
        maxGamingDays = BsonUtil.intValue(src, "mgd").orElse(0);
        ip = BsonUtil.stringValue(src, "ip").orElse("");
        loginTime = BsonUtil.dateTimeValue(src, "lgt").orElse(null);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("cnt").value(), count));
        }
        if (updatedFields.get(2)) {
            updates.add(Updates.set(xpath().resolve("d").value(), days));
        }
        if (updatedFields.get(3)) {
            updates.add(Updates.set(xpath().resolve("cnd").value(), continuousDays));
        }
        if (updatedFields.get(4)) {
            updates.add(Updates.set(xpath().resolve("mcd").value(), maxContinuousDays));
        }
        if (updatedFields.get(5)) {
            updates.add(Updates.set(xpath().resolve("gmd").value(), gamingDays));
        }
        if (updatedFields.get(6)) {
            updates.add(Updates.set(xpath().resolve("mgd").value(), maxGamingDays));
        }
        if (updatedFields.get(7)) {
            updates.add(Updates.set(xpath().resolve("ip").value(), ip));
        }
        if (updatedFields.get(8)) {
            updates.add(Updates.set(xpath().resolve("lgt").value(), BsonUtil.toBsonDateTime(loginTime)));
        }
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("count", count);
        }
        if (updatedFields.get(2)) {
            update.put("days", days);
        }
        if (updatedFields.get(3)) {
            update.put("continuousDays", continuousDays);
        }
        if (updatedFields.get(4)) {
            update.put("maxContinuousDays", maxContinuousDays);
        }
        if (updatedFields.get(5)) {
            update.put("gamingDays", gamingDays);
        }
        if (updatedFields.get(6)) {
            update.put("maxGamingDays", maxGamingDays);
        }
        if (updatedFields.get(7)) {
            update.put("ip", ip);
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        return Map.of();
    }

    @Override
    protected int deletedSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "LoginInfo(" + "count=" + count + ", " + "days=" + days + ", " + "continuousDays=" + continuousDays + ", " + "maxContinuousDays=" + maxContinuousDays + ", " + "gamingDays=" + gamingDays + ", " + "maxGamingDays=" + maxGamingDays + ", " + "ip=" + ip + ", " + "loginTime=" + loginTime + ")";
    }

}
