package com.github.fmjsjx.demo.http.entity.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.bson.BsonUtil;
import com.github.fmjsjx.libcommon.bson.DotNotation;
import com.github.fmjsjx.libcommon.bson.model.ObjectModel;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class GuideInfo extends ObjectModel<GuideInfo> {

    private static final DotNotation XPATH = DotNotation.of("gd");

    private final Player parent;

    private int status;

    public GuideInfo(Player parent) {
        this.parent = parent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (this.status != status) {
            this.status = status;
            updatedFields.set(1);
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
        bson.append("s", new BsonInt32(status));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("s", status);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("s", status);
        return data;
    }

    @Override
    public void load(Document src) {
        status = BsonUtil.intValue(src, "s").orElse(0);
    }

    @Override
    public void load(BsonDocument src) {
        status = BsonUtil.intValue(src, "s").orElse(0);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        status = BsonUtil.intValue(src, "s").orElse(0);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        status = BsonUtil.intValue(src, "s").orElse(0);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("s").value(), status));
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
            update.put("status", status);
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
        return "GuideInfo(" + "status=" + status + ")";
    }

}
