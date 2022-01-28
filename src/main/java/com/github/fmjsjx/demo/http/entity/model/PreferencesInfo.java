package com.github.fmjsjx.demo.http.entity.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DotNotation;
import com.github.fmjsjx.bson.model.core.ObjectModel;
import com.github.fmjsjx.bson.model.core.SimpleMapModel;
import com.github.fmjsjx.bson.model.core.SimpleValueTypes;
import com.github.fmjsjx.libcommon.collection.ListSet;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;
import org.bson.BsonArray;

public class PreferencesInfo extends ObjectModel<PreferencesInfo> {

    public static final String BNAME_CUSTOM = "ctm";
    public static final String BNAME_FEATURES = "fts";
    public static final String BNAME_ATTRIBUTES = "atr";

    private static final DotNotation XPATH = DotNotation.of("pfc");

    private final Player parent;

    private String custom;
    private ListSet<String> features;
    private final SimpleMapModel<String, String, PreferencesInfo> attributes = SimpleMapModel.stringKeys(this, "atr", SimpleValueTypes.STRING);

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

    public ListSet<String> getFeatures() {
        return features;
    }

    public void setFeatures(Collection<String> features) {
        if (features == null) {
            this.features = null;
        } else {
            this.features = ListSet.copyOf(features);
        }
        updatedFields.set(2);
    }

    public SimpleMapModel<String, String, PreferencesInfo> getAttributes() {
        return attributes;
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
        if (attributes.updated()) {
            return true;
        }
        return super.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("ctm", new BsonString(custom));
        var features = this.features;
        if (features != null) {
            var featuresArray = new BsonArray(features.size());
            features.stream().map(SimpleValueTypes.STRING::toBson).forEach(featuresArray::add);
            bson.append("fts", featuresArray);
        }
        bson.append("atr", attributes.toBson());
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("ctm", custom);
        var features = this.features;
        if (features != null) {
            doc.append("fts", features.internalList());
        }
        doc.append("atr", attributes.toDocument());
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("ctm", custom);
        var features = this.features;
        if (features != null) {
            data.put("fts", features);
        }
        data.put("atr", attributes.toData());
        return data;
    }

    @Override
    public void load(Document src) {
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
        features = BsonUtil.listValue(src, "fts").map(featuresList -> {
            return ListSet.copyOf(featuresList.stream().map(SimpleValueTypes.STRING::cast).collect(Collectors.toList()));
        }).orElse(null);
        BsonUtil.documentValue(src, "atr").ifPresentOrElse(attributes::load, attributes::clear);
    }

    @Override
    public void load(BsonDocument src) {
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
        features = BsonUtil.arrayValue(src, "fts").map(featuresArray -> {
            var featuresList = featuresArray.stream().map(SimpleValueTypes.STRING::parse).collect(Collectors.toList());
            return ListSet.copyOf(featuresList);
        }).orElse(null);
        BsonUtil.documentValue(src, "atr").ifPresentOrElse(attributes::load, attributes::clear);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
        features = BsonUtil.arrayValue(src, "fts").filter(featuresAny -> featuresAny.valueType() == ValueType.ARRAY).map(featuresAny -> {
            var featuresList = new ArrayList<String>(featuresAny.size());
            for (var featuresAnyElement : featuresAny) {
                featuresList.add(SimpleValueTypes.STRING.parse(featuresAnyElement));
            }
            return ListSet.copyOf(featuresList);
        }).orElse(null);
        BsonUtil.objectValue(src, "atr").ifPresentOrElse(attributes::load, attributes::clear);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        custom = BsonUtil.stringValue(src, "ctm").orElse("");
        features = BsonUtil.arrayValue(src, "fts").filter(JsonNode::isArray).map(featuresNode -> {
            var featuresList = new ArrayList<String>(featuresNode.size());
            for (var featuresNodeElement : featuresNode) {
                featuresList.add(SimpleValueTypes.STRING.parse(featuresNodeElement));
            }
            return ListSet.copyOf(featuresList);
        }).orElse(null);
        BsonUtil.objectValue(src, "atr").ifPresentOrElse(attributes::load, attributes::clear);
    }

    public boolean customUpdated() {
        return updatedFields.get(1);
    }

    public boolean featuresUpdated() {
        return updatedFields.get(2);
    }

    public boolean attributesUpdated() {
        return attributes.updated();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("ctm").value(), custom));
        }
        if (updatedFields.get(2)) {
            var features = this.features;
            if (features == null) {
                updates.add(Updates.unset(xpath().resolve("fts").value()));
            } else {
                var featuresArray = new BsonArray(features.size());
                features.stream().map(SimpleValueTypes.STRING::toBson).forEach(featuresArray::add);
                updates.add(Updates.set(xpath().resolve("fts").value(), featuresArray));
            }
        }
        var attributes = this.attributes;
        if (attributes.updated()) {
            attributes.appendUpdates(updates);
        }
    }

    @Override
    protected void resetChildren() {
        attributes.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("custom", custom);
        }
        if (updatedFields.get(2) && features != null) {
            update.put("features", features);
        }
        if (attributes.updated()) {
            update.put("attributes", attributes.toUpdate());
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var delete = new LinkedHashMap<>();
        if (updatedFields.get(2) && features == null) {
            delete.put("features", 1);
        }
        var attributes = this.attributes;
        if (attributes.deletedSize() > 0) {
            delete.put("attributes", attributes.toDelete());
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (updatedFields.get(2) && features == null) {
            n++;
        }
        if (attributes.deletedSize() > 0) {
            n++;
        }
        return n;
    }

    public boolean includeFeature(String feature) {
        return features != null && features.contains(feature);
    }

    public boolean excludeFeature(String feature) {
        return !includeFeature(feature);
    }

    @Override
    public String toString() {
        return "PreferencesInfo(" + "custom=" + custom + ", " + "features=" + features + ", " + "attributes=" + attributes + ")";
    }

}
