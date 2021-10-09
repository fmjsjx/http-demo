package com.github.fmjsjx.demo.http.entity.model;

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
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class StatisticsInfo extends ObjectModel<StatisticsInfo> {

    public static final String BNAME_VIDEO_COUNT = "vct";
    public static final String BNAME_VIDEO_COUNTS = "vcs";
    public static final String BNAME_GAMING_COUNT = "gct";

    private static final DotNotation XPATH = DotNotation.of("stc");

    private final Player parent;

    private int videoCount;
    @JsonIgnore
    private final SimpleMapModel<Integer, Integer, StatisticsInfo> videoCounts = SimpleMapModel.integerKeys(this, "vcs", SimpleValueTypes.INTEGER);
    private int gamingCount;

    public StatisticsInfo(Player parent) {
        this.parent = parent;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        if (this.videoCount != videoCount) {
            this.videoCount = videoCount;
            updatedFields.set(1);
        }
    }

    public int increaseVideoCount() {
        var videoCount = this.videoCount += 1;
        updatedFields.set(1);
        return videoCount;
    }

    @JsonIgnore
    public SimpleMapModel<Integer, Integer, StatisticsInfo> getVideoCounts() {
        return videoCounts;
    }

    public int getGamingCount() {
        return gamingCount;
    }

    public void setGamingCount(int gamingCount) {
        if (this.gamingCount != gamingCount) {
            this.gamingCount = gamingCount;
            updatedFields.set(3);
        }
    }

    public int increaseGamingCount() {
        var gamingCount = this.gamingCount += 1;
        updatedFields.set(3);
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
        bson.append("vct", new BsonInt32(videoCount));
        bson.append("vcs", videoCounts.toBson());
        bson.append("gct", new BsonInt32(gamingCount));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("vct", videoCount);
        doc.append("vcs", videoCounts.toDocument());
        doc.append("gct", gamingCount);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("vct", videoCount);
        data.put("vcs", videoCounts.toData());
        data.put("gct", gamingCount);
        return data;
    }

    @Override
    public void load(Document src) {
        videoCount = BsonUtil.intValue(src, "vct").orElse(0);
        BsonUtil.documentValue(src, "vcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    @Override
    public void load(BsonDocument src) {
        videoCount = BsonUtil.intValue(src, "vct").orElse(0);
        BsonUtil.documentValue(src, "vcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        videoCount = BsonUtil.intValue(src, "vct").orElse(0);
        BsonUtil.objectValue(src, "vcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        videoCount = BsonUtil.intValue(src, "vct").orElse(0);
        BsonUtil.objectValue(src, "vcs").ifPresentOrElse(videoCounts::load, videoCounts::clear);
        gamingCount = BsonUtil.intValue(src, "gct").orElse(0);
    }

    public boolean videoCountUpdated() {
        return updatedFields.get(1);
    }

    public boolean videoCountsUpdated() {
        return videoCounts.updated();
    }

    public boolean gamingCountUpdated() {
        return updatedFields.get(3);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("vct").value(), videoCount));
        }
        var videoCounts = this.videoCounts;
        if (videoCounts.updated()) {
            videoCounts.appendUpdates(updates);
        }
        if (updatedFields.get(3)) {
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
        if (updatedFields.get(1)) {
            update.put("videoCount", videoCount);
        }
        if (updatedFields.get(3)) {
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
        return "StatisticsInfo(" + "videoCount=" + videoCount + ", " + "videoCounts=" + videoCounts + ", " + "gamingCount=" + gamingCount + ")";
    }

}
