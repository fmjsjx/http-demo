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
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class WalletInfo extends ObjectModel<WalletInfo> {

    public static final String BNAME_COIN_TOTAL = "ct";
    public static final String BNAME_COIN_USED = "cu";
    public static final String BNAME_DIAMOND = "d";

    private static final DotNotation XPATH = DotNotation.of("wlt");

    private final Player parent;

    private int coinTotal;
    @JsonIgnore
    private int coinUsed;
    private int diamond;

    public WalletInfo(Player parent) {
        this.parent = parent;
    }

    public int getCoinTotal() {
        return coinTotal;
    }

    public void setCoinTotal(int coinTotal) {
        if (this.coinTotal != coinTotal) {
            this.coinTotal = coinTotal;
            updatedFields.set(1);
            updatedFields.set(3);
        }
    }

    @JsonIgnore
    public int getCoinUsed() {
        return coinUsed;
    }

    public void setCoinUsed(int coinUsed) {
        if (this.coinUsed != coinUsed) {
            this.coinUsed = coinUsed;
            updatedFields.set(2);
            updatedFields.set(3);
        }
    }

    public int getCoin() {
        return coinTotal - coinUsed;
    }

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        if (this.diamond != diamond) {
            this.diamond = diamond;
            updatedFields.set(4);
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
        bson.append("ct", new BsonInt32(coinTotal));
        bson.append("cu", new BsonInt32(coinUsed));
        bson.append("d", new BsonInt32(diamond));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("ct", coinTotal);
        doc.append("cu", coinUsed);
        doc.append("d", diamond);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("ct", coinTotal);
        data.put("cu", coinUsed);
        data.put("d", diamond);
        return data;
    }

    @Override
    public void load(Document src) {
        coinTotal = BsonUtil.intValue(src, "ct").orElse(0);
        coinUsed = BsonUtil.intValue(src, "cu").orElse(0);
        diamond = BsonUtil.intValue(src, "d").orElse(0);
    }

    @Override
    public void load(BsonDocument src) {
        coinTotal = BsonUtil.intValue(src, "ct").orElse(0);
        coinUsed = BsonUtil.intValue(src, "cu").orElse(0);
        diamond = BsonUtil.intValue(src, "d").orElse(0);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        coinTotal = BsonUtil.intValue(src, "ct").orElse(0);
        coinUsed = BsonUtil.intValue(src, "cu").orElse(0);
        diamond = BsonUtil.intValue(src, "d").orElse(0);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        coinTotal = BsonUtil.intValue(src, "ct").orElse(0);
        coinUsed = BsonUtil.intValue(src, "cu").orElse(0);
        diamond = BsonUtil.intValue(src, "d").orElse(0);
    }

    public boolean coinTotalUpdated() {
        return updatedFields.get(1);
    }

    public boolean coinUsedUpdated() {
        return updatedFields.get(2);
    }

    public boolean coinUpdated() {
        return updatedFields.get(3);
    }

    public boolean diamondUpdated() {
        return updatedFields.get(4);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("ct").value(), coinTotal));
        }
        if (updatedFields.get(2)) {
            updates.add(Updates.set(xpath().resolve("cu").value(), coinUsed));
        }
        if (updatedFields.get(4)) {
            updates.add(Updates.set(xpath().resolve("d").value(), diamond));
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
            update.put("coinTotal", coinTotal);
        }
        if (updatedFields.get(3)) {
            update.put("coin", getCoin());
        }
        if (updatedFields.get(4)) {
            update.put("diamond", diamond);
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
        return "WalletInfo(" + "coinTotal=" + coinTotal + ", " + "coinUsed=" + coinUsed + ", " + "diamond=" + diamond + ")";
    }

}
