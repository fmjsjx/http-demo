package com.github.fmjsjx.demo.http.entity.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.bson.BsonUtil;
import com.github.fmjsjx.libcommon.bson.DotNotation;
import com.github.fmjsjx.libcommon.bson.model.ObjectModel;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class BasicInfo extends ObjectModel<BasicInfo> {

    private static final DotNotation XPATH = DotNotation.of("bsc");

    private final Player parent;

    private String nickname;
    private int faceId;
    private String faceUrl;

    public BasicInfo(Player parent) {
        this.parent = parent;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        if (ObjectUtil.isNotEquals(this.nickname, nickname)) {
            this.nickname = nickname;
            updatedFields.set(1);
        }
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        if (this.faceId != faceId) {
            this.faceId = faceId;
            updatedFields.set(2);
        }
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        if (ObjectUtil.isNotEquals(this.faceUrl, faceUrl)) {
            this.faceUrl = faceUrl;
            updatedFields.set(3);
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
        bson.append("nn", new BsonString(nickname));
        bson.append("fi", new BsonInt32(faceId));
        bson.append("fu", new BsonString(faceUrl));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("nn", nickname);
        doc.append("fi", faceId);
        doc.append("fu", faceUrl);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("nn", nickname);
        data.put("fi", faceId);
        data.put("fu", faceUrl);
        return data;
    }

    @Override
    public void load(Document src) {
        nickname = BsonUtil.stringValue(src, "nn").orElse("");
        faceId = BsonUtil.intValue(src, "fi").orElse(0);
        faceUrl = BsonUtil.stringValue(src, "fu").orElse("");
    }

    @Override
    public void load(BsonDocument src) {
        nickname = BsonUtil.stringValue(src, "nn").orElse("");
        faceId = BsonUtil.intValue(src, "fi").orElse(0);
        faceUrl = BsonUtil.stringValue(src, "fu").orElse("");
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        nickname = BsonUtil.stringValue(src, "nn").orElse("");
        faceId = BsonUtil.intValue(src, "fi").orElse(0);
        faceUrl = BsonUtil.stringValue(src, "fu").orElse("");
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        nickname = BsonUtil.stringValue(src, "nn").orElse("");
        faceId = BsonUtil.intValue(src, "fi").orElse(0);
        faceUrl = BsonUtil.stringValue(src, "fu").orElse("");
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("nn").value(), nickname));
        }
        if (updatedFields.get(2)) {
            updates.add(Updates.set(xpath().resolve("fi").value(), faceId));
        }
        if (updatedFields.get(3)) {
            updates.add(Updates.set(xpath().resolve("fu").value(), faceUrl));
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
            update.put("nickname", nickname);
        }
        if (updatedFields.get(2)) {
            update.put("faceId", faceId);
        }
        if (updatedFields.get(3)) {
            update.put("faceUrl", faceUrl);
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
        return "BasicInfo(" + "nickname=" + nickname + ", " + "faceId=" + faceId + ", " + "faceUrl=" + faceUrl + ")";
    }

}
