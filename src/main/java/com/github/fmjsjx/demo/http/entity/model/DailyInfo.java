package com.github.fmjsjx.demo.http.entity.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DotNotation;
import com.github.fmjsjx.bson.model.core.ObjectModel;
import com.github.fmjsjx.bson.model.core.SimpleMapModel;
import com.github.fmjsjx.bson.model.core.SimpleValueTypes;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class DailyInfo extends ObjectModel<DailyInfo> {

    public static final String BNAME_DAY = "day";
    public static final String BNAME_COIN = "cn";
    public static final String BNAME_DIAMOND = "dm";
    public static final String BNAME_VIDEO_COUNT = "vdc";
    public static final String BNAME_VIDEO_COUNTS = "vdcs";
    public static final String BNAME_GAMING_COUNT = "gct";

    private static final DotNotation XPATH = DotNotation.of("dly");

    private final Player parent;

    @JsonIgnore
    private LocalDate day;
    private int coin;
    private int diamond;
    private int videoCount;
    @JsonIgnore
    private final SimpleMapModel<Integer, Integer, DailyInfo> videoCounts = SimpleMapModel.integerKeys(this, "vdcs", SimpleValueTypes.INTEGER);
    private int gamingCount;

    public DailyInfo(Player parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        if (ObjectUtil.isNotEquals(this.day, day)) {
            this.day = day;
            updatedFields.set(1);
        }
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        if (this.coin != coin) {
            this.coin = coin;
            updatedFields.set(2);
        }
    }

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        if (this.diamond != diamond) {
            this.diamond = diamond;
            updatedFields.set(3);
        }
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        if (this.videoCount != videoCount) {
            this.videoCount = videoCount;
            updatedFields.set(4);
        }
    }

    public int increaseVideoCount() {
        var videoCount = this.videoCount += 1;
        updatedFields.set(4);
        return videoCount;
    }

    @JsonIgnore
    public SimpleMapModel<Integer, Integer, DailyInfo> getVideoCounts() {
        return videoCounts;
    }

    public int getGamingCount() {
        return gamingCount;
    }

    public void setGamingCount(int gamingCount) {
        if (this.gamingCount != gamingCount) {
            this.gamingCount = gamingCount;
            updatedFields.set(6);
        }
    }

    public int increaseGamingCount() {
        var gamingCount = this.gamingCount += 1;
        updatedFields.set(6);
        return gamingCount;
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
    public boolean updated() {
        if (videoCounts.updated()) {
            return true;
        }
        return super.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("day", new BsonInt32(DateTimeUtil.toNumber(day)));
        bson.append("cn", new BsonInt32(coin));
        bson.append("dm", new BsonInt32(diamond));
        bson.append("vdc", new BsonInt32(videoCount));
        bson.append("vdcs", videoCounts.toBson());
        bson.append("gct", new BsonInt32(gamingCount));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("day", DateTimeUtil.toNumber(day));
        doc.append("cn", coin);
        doc.append("dm", diamond);
        doc.append("vdc", videoCount);
        doc.append("vdcs", videoCounts.toDocument());
        doc.append("gct", gamingCount);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("day", DateTimeUtil.toNumber(day));
        data.put("cn", coin);
        data.put("dm", diamond);
        data.put("vdc", videoCount);
        data.put("vdcs", videoCounts.toData());
        data.put("gct", gamingCount);
        return data;
    }

    @Override
    public void load(Document src) {
        day = DateTimeUtil.toDate(BsonUtil.intValue(src, "day").getAsInt());
        coin = BsonUtil.intValue(src, "cn").orElse(0);
        diamond = BsonUtil.intValue(src, "dm").orElse(0);
        videoCount = BsonUtil.intValue(src, "vdc").orElse(0);
        BsonUtil.documentValue(src, "vdcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    @Override
    public void load(BsonDocument src) {
        day = DateTimeUtil.toDate(BsonUtil.intValue(src, "day").getAsInt());
        coin = BsonUtil.intValue(src, "cn").orElse(0);
        diamond = BsonUtil.intValue(src, "dm").orElse(0);
        videoCount = BsonUtil.intValue(src, "vdc").orElse(0);
        BsonUtil.documentValue(src, "vdcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        day = DateTimeUtil.toDate(BsonUtil.intValue(src, "day").getAsInt());
        coin = BsonUtil.intValue(src, "cn").orElse(0);
        diamond = BsonUtil.intValue(src, "dm").orElse(0);
        videoCount = BsonUtil.intValue(src, "vdc").orElse(0);
        BsonUtil.objectValue(src, "vdcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        day = DateTimeUtil.toDate(BsonUtil.intValue(src, "day").getAsInt());
        coin = BsonUtil.intValue(src, "cn").orElse(0);
        diamond = BsonUtil.intValue(src, "dm").orElse(0);
        videoCount = BsonUtil.intValue(src, "vdc").orElse(0);
        BsonUtil.objectValue(src, "vdcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    public boolean dayUpdated() {
        return updatedFields.get(1);
    }

    public boolean coinUpdated() {
        return updatedFields.get(2);
    }

    public boolean diamondUpdated() {
        return updatedFields.get(3);
    }

    public boolean videoCountUpdated() {
        return updatedFields.get(4);
    }

    public boolean videoCountsUpdated() {
        return videoCounts.updated();
    }

    public boolean gamingCountUpdated() {
        return updatedFields.get(6);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("day").value(), DateTimeUtil.toNumber(day)));
        }
        if (updatedFields.get(2)) {
            updates.add(Updates.set(xpath().resolve("cn").value(), coin));
        }
        if (updatedFields.get(3)) {
            updates.add(Updates.set(xpath().resolve("dm").value(), diamond));
        }
        if (updatedFields.get(4)) {
            updates.add(Updates.set(xpath().resolve("vdc").value(), videoCount));
        }
        var videoCounts = this.videoCounts;
        if (videoCounts.updated()) {
            videoCounts.appendUpdates(updates);
        }
        if (updatedFields.get(6)) {
            updates.add(Updates.set(xpath().resolve("gct").value(), gamingCount));
        }
    }

    @Override
    protected void resetChildren() {
        videoCounts.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(2)) {
            update.put("coin", coin);
        }
        if (updatedFields.get(3)) {
            update.put("diamond", diamond);
        }
        if (updatedFields.get(4)) {
            update.put("videoCount", videoCount);
        }
        if (updatedFields.get(6)) {
            update.put("gamingCount", gamingCount);
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
        return "DailyInfo(" + "day=" + day + ", " + "coin=" + coin + ", " + "diamond=" + diamond + ", " + "videoCount=" + videoCount + ", " + "videoCounts=" + videoCounts + ", " + "gamingCount=" + gamingCount + ")";
    }

}
