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
import com.github.fmjsjx.libcommon.bson.BsonUtil;
import com.github.fmjsjx.libcommon.bson.DotNotation;
import com.github.fmjsjx.libcommon.bson.model.ObjectModel;
import com.github.fmjsjx.libcommon.bson.model.SimpleMapModel;
import com.github.fmjsjx.libcommon.bson.model.SimpleValueTypes;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class VideosInfo extends ObjectModel<VideosInfo> {

    private static final DotNotation XPATH = DotNotation.of("vds");

    private final Player parent;

    private int count;
    @JsonIgnore
    private final SimpleMapModel<Integer, Integer, VideosInfo> counts = SimpleMapModel.integerKeys(this, "cns", SimpleValueTypes.INTEGER);

    public VideosInfo(Player parent) {
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

    @JsonIgnore
    public SimpleMapModel<Integer, Integer, VideosInfo> getCounts() {
        return counts;
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
        if (counts.updated()) {
            return true;
        }
        return super.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("cnt", new BsonInt32(count));
        bson.append("cns", counts.toBson());
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("cnt", count);
        doc.append("cns", counts.toDocument());
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("cnt", count);
        data.put("cns", counts.toData());
        return data;
    }

    @Override
    public void load(Document src) {
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        BsonUtil.documentValue(src, "cns").ifPresentOrElse(counts::load, counts::clear);
    }

    @Override
    public void load(BsonDocument src) {
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        BsonUtil.documentValue(src, "cns").ifPresentOrElse(counts::load, counts::clear);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        BsonUtil.objectValue(src, "cns").ifPresentOrElse(counts::load, counts::clear);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        count = BsonUtil.intValue(src, "cnt").orElse(0);
        BsonUtil.objectValue(src, "cns").ifPresentOrElse(counts::load, counts::clear);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("cnt").value(), count));
        }
        var counts = this.counts;
        if (counts.updated()) {
            counts.appendUpdates(updates);
        }
    }

    @Override
    protected void resetChildren() {
        counts.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("count", count);
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
        return "VideosInfo(" + "count=" + count + ", " + "counts=" + counts + ")";
    }

}
