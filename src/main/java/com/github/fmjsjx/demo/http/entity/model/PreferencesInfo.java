package com.github.fmjsjx.demo.http.entity.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
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

public class PreferencesInfo extends ObjectModel<PreferencesInfo> {

    private static final DotNotation XPATH = DotNotation.of("pfc");

    private final Player parent;

    private String custom;

    public PreferencesInfo(Player parent) {
        this.parent = parent;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        if (ObjectUtil.isNotEquals(this.custom, custom)) {
            this.custom = custom;
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
        bson.append("ctm", new BsonString(custom));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("ctm", custom);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("ctm", custom);
        return data;
    }

    @Override
    public void load(Document src) {
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
    }

    @Override
    public void load(BsonDocument src) {
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("ctm").value(), custom));
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
            update.put("custom", custom);
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
        return "PreferencesInfo(" + "custom=" + custom + ")";
    }

}
