package com.github.fmjsjx.demo.http.entity.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.bson.BsonUtil;
import com.github.fmjsjx.libcommon.bson.model.RootModel;
import com.github.fmjsjx.libcommon.bson.model.SimpleMapModel;
import com.github.fmjsjx.libcommon.bson.model.SimpleValueTypes;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class Player extends RootModel<Player> {

    private int uid;
    private final PreferencesInfo preferences = new PreferencesInfo(this);
    private final BasicInfo basic = new BasicInfo(this);
    private final LoginInfo login = new LoginInfo(this);
    private final GuideInfo guide = new GuideInfo(this);
    private final WalletInfo wallet = new WalletInfo(this);
    private final SimpleMapModel<Integer, Integer, Player> items = SimpleMapModel.integerKeys(this, "itm", SimpleValueTypes.INTEGER);
    private final VideosInfo videos = new VideosInfo(this);
    private final DailyInfo daily = new DailyInfo(this);
    @JsonIgnore
    private int updateVersion;
    @JsonIgnore
    private LocalDateTime createTime;
    @JsonIgnore
    private LocalDateTime updateTime;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        if (this.uid != uid) {
            this.uid = uid;
            updatedFields.set(1);
        }
    }

    public PreferencesInfo getPreferences() {
        return preferences;
    }

    public BasicInfo getBasic() {
        return basic;
    }

    public LoginInfo getLogin() {
        return login;
    }

    public GuideInfo getGuide() {
        return guide;
    }

    public WalletInfo getWallet() {
        return wallet;
    }

    public SimpleMapModel<Integer, Integer, Player> getItems() {
        return items;
    }

    public VideosInfo getVideos() {
        return videos;
    }

    public DailyInfo getDaily() {
        return daily;
    }

    @JsonIgnore
    public int getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(int updateVersion) {
        if (this.updateVersion != updateVersion) {
            this.updateVersion = updateVersion;
            updatedFields.set(10);
        }
    }

    public int increaseUpdateVersion() {
        var updateVersion = this.updateVersion += 1;
        updatedFields.set(10);
        return updateVersion;
    }

    @JsonIgnore
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        if (ObjectUtil.isNotEquals(this.createTime, createTime)) {
            this.createTime = createTime;
            updatedFields.set(11);
        }
    }

    @JsonIgnore
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        if (ObjectUtil.isNotEquals(this.updateTime, updateTime)) {
            this.updateTime = updateTime;
            updatedFields.set(12);
        }
    }

    @Override
    public boolean updated() {
        if (preferences.updated() || basic.updated() || login.updated() || guide.updated() || wallet.updated() || items.updated() || videos.updated() || daily.updated()) {
            return true;
        }
        return super.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("_id", new BsonInt32(uid));
        bson.append("pfc", preferences.toBson());
        bson.append("bsc", basic.toBson());
        bson.append("lgn", login.toBson());
        bson.append("gd", guide.toBson());
        bson.append("wlt", wallet.toBson());
        bson.append("itm", items.toBson());
        bson.append("vds", videos.toBson());
        bson.append("dly", daily.toBson());
        bson.append("_uv", new BsonInt32(updateVersion));
        if (createTime != null) {
            bson.append("_ct", BsonUtil.toBsonDateTime(createTime));
        }
        if (updateTime != null) {
            bson.append("_ut", BsonUtil.toBsonDateTime(updateTime));
        }
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("_id", uid);
        doc.append("pfc", preferences.toDocument());
        doc.append("bsc", basic.toDocument());
        doc.append("lgn", login.toDocument());
        doc.append("gd", guide.toDocument());
        doc.append("wlt", wallet.toDocument());
        doc.append("itm", items.toDocument());
        doc.append("vds", videos.toDocument());
        doc.append("dly", daily.toDocument());
        doc.append("_uv", updateVersion);
        if (createTime != null) {
            doc.append("_ct", DateTimeUtil.toLegacyDate(createTime));
        }
        if (updateTime != null) {
            doc.append("_ut", DateTimeUtil.toLegacyDate(updateTime));
        }
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("_id", uid);
        data.put("pfc", preferences.toData());
        data.put("bsc", basic.toData());
        data.put("lgn", login.toData());
        data.put("gd", guide.toData());
        data.put("wlt", wallet.toData());
        data.put("itm", items.toData());
        data.put("vds", videos.toData());
        data.put("dly", daily.toData());
        data.put("_uv", updateVersion);
        if (createTime != null) {
            data.put("_ct", DateTimeUtil.toEpochMilli(createTime));
        }
        if (updateTime != null) {
            data.put("_ut", DateTimeUtil.toEpochMilli(updateTime));
        }
        return data;
    }

    @Override
    public void load(Document src) {
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.documentValue(src, "pfc").ifPresentOrElse(preferences::load, preferences::reset);
        BsonUtil.documentValue(src, "bsc").ifPresentOrElse(basic::load, basic::reset);
        BsonUtil.documentValue(src, "lgn").ifPresentOrElse(login::load, login::reset);
        BsonUtil.documentValue(src, "gd").ifPresentOrElse(guide::load, guide::reset);
        BsonUtil.documentValue(src, "wlt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.documentValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.documentValue(src, "vds").ifPresentOrElse(videos::load, videos::reset);
        BsonUtil.documentValue(src, "dly").ifPresentOrElse(daily::load, daily::reset);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").orElse(null);
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElse(null);
        reset();
    }

    @Override
    public void load(BsonDocument src) {
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.documentValue(src, "pfc").ifPresentOrElse(preferences::load, preferences::reset);
        BsonUtil.documentValue(src, "bsc").ifPresentOrElse(basic::load, basic::reset);
        BsonUtil.documentValue(src, "lgn").ifPresentOrElse(login::load, login::reset);
        BsonUtil.documentValue(src, "gd").ifPresentOrElse(guide::load, guide::reset);
        BsonUtil.documentValue(src, "wlt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.documentValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.documentValue(src, "vds").ifPresentOrElse(videos::load, videos::reset);
        BsonUtil.documentValue(src, "dly").ifPresentOrElse(daily::load, daily::reset);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").orElse(null);
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElse(null);
        reset();
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.objectValue(src, "pfc").ifPresentOrElse(preferences::load, preferences::reset);
        BsonUtil.objectValue(src, "bsc").ifPresentOrElse(basic::load, basic::reset);
        BsonUtil.objectValue(src, "lgn").ifPresentOrElse(login::load, login::reset);
        BsonUtil.objectValue(src, "gd").ifPresentOrElse(guide::load, guide::reset);
        BsonUtil.objectValue(src, "wlt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.objectValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.objectValue(src, "vds").ifPresentOrElse(videos::load, videos::reset);
        BsonUtil.objectValue(src, "dly").ifPresentOrElse(daily::load, daily::reset);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").orElse(null);
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElse(null);
        reset();
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.objectValue(src, "pfc").ifPresentOrElse(preferences::load, preferences::reset);
        BsonUtil.objectValue(src, "bsc").ifPresentOrElse(basic::load, basic::reset);
        BsonUtil.objectValue(src, "lgn").ifPresentOrElse(login::load, login::reset);
        BsonUtil.objectValue(src, "gd").ifPresentOrElse(guide::load, guide::reset);
        BsonUtil.objectValue(src, "wlt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.objectValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.objectValue(src, "vds").ifPresentOrElse(videos::load, videos::reset);
        BsonUtil.objectValue(src, "dly").ifPresentOrElse(daily::load, daily::reset);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").orElse(null);
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElse(null);
        reset();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set("_id", uid));
        }
        var preferences = this.preferences;
        if (preferences.updated()) {
            preferences.appendUpdates(updates);
        }
        var basic = this.basic;
        if (basic.updated()) {
            basic.appendUpdates(updates);
        }
        var login = this.login;
        if (login.updated()) {
            login.appendUpdates(updates);
        }
        var guide = this.guide;
        if (guide.updated()) {
            guide.appendUpdates(updates);
        }
        var wallet = this.wallet;
        if (wallet.updated()) {
            wallet.appendUpdates(updates);
        }
        var items = this.items;
        if (items.updated()) {
            items.appendUpdates(updates);
        }
        var videos = this.videos;
        if (videos.updated()) {
            videos.appendUpdates(updates);
        }
        var daily = this.daily;
        if (daily.updated()) {
            daily.appendUpdates(updates);
        }
        if (updatedFields.get(10)) {
            updates.add(Updates.set("_uv", updateVersion));
        }
        if (updatedFields.get(11)) {
            updates.add(Updates.set("_ct", BsonUtil.toBsonDateTime(createTime)));
        }
        if (updatedFields.get(12)) {
            updates.add(Updates.set("_ut", BsonUtil.toBsonDateTime(updateTime)));
        }
    }

    @Override
    protected void resetChildren() {
        preferences.reset();
        basic.reset();
        login.reset();
        guide.reset();
        wallet.reset();
        items.reset();
        videos.reset();
        daily.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("uid", uid);
        }
        if (preferences.updated()) {
            update.put("preferences", preferences.toUpdate());
        }
        if (basic.updated()) {
            update.put("basic", basic.toUpdate());
        }
        if (login.updated()) {
            update.put("login", login.toUpdate());
        }
        if (guide.updated()) {
            update.put("guide", guide.toUpdate());
        }
        if (wallet.updated()) {
            update.put("wallet", wallet.toUpdate());
        }
        if (items.updated()) {
            update.put("items", items.toUpdate());
        }
        if (videos.updated()) {
            update.put("videos", videos.toUpdate());
        }
        if (daily.updated()) {
            update.put("daily", daily.toUpdate());
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var delete = new LinkedHashMap<>();
        var preferences = this.preferences;
        if (preferences.deletedSize() > 0) {
            delete.put("preferences", preferences.toDelete());
        }
        var basic = this.basic;
        if (basic.deletedSize() > 0) {
            delete.put("basic", basic.toDelete());
        }
        var login = this.login;
        if (login.deletedSize() > 0) {
            delete.put("login", login.toDelete());
        }
        var guide = this.guide;
        if (guide.deletedSize() > 0) {
            delete.put("guide", guide.toDelete());
        }
        var wallet = this.wallet;
        if (wallet.deletedSize() > 0) {
            delete.put("wallet", wallet.toDelete());
        }
        var items = this.items;
        if (items.deletedSize() > 0) {
            delete.put("items", items.toDelete());
        }
        var videos = this.videos;
        if (videos.deletedSize() > 0) {
            delete.put("videos", videos.toDelete());
        }
        var daily = this.daily;
        if (daily.deletedSize() > 0) {
            delete.put("daily", daily.toDelete());
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (preferences.deletedSize() > 0) {
            n++;
        }
        if (basic.deletedSize() > 0) {
            n++;
        }
        if (login.deletedSize() > 0) {
            n++;
        }
        if (guide.deletedSize() > 0) {
            n++;
        }
        if (wallet.deletedSize() > 0) {
            n++;
        }
        if (items.deletedSize() > 0) {
            n++;
        }
        if (videos.deletedSize() > 0) {
            n++;
        }
        if (daily.deletedSize() > 0) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        return "Player(" + "uid=" + uid + ", " + "preferences=" + preferences + ", " + "basic=" + basic + ", " + "login=" + login + ", " + "guide=" + guide + ", " + "wallet=" + wallet + ", " + "items=" + items + ", " + "videos=" + videos + ", " + "daily=" + daily + ", " + "updateVersion=" + updateVersion + ", " + "createTime=" + createTime + ", " + "updateTime=" + updateTime + ")";
    }

}
