package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.core.config.AdvertConfig.AdvertShard;
import com.github.fmjsjx.libcommon.collection.IntHashSet;
import com.github.fmjsjx.libcommon.collection.IntSet;
import com.github.fmjsjx.libcommon.collection.PrimitiveCollections;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;

@ToString
public class AdvertConfig implements ShardingConfig<AdvertShard> {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final AdvertConfig loadFromYaml(InputStream in) {
        return new AdvertConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;
    @ToString.Exclude
    final AdvertShard[] slots = new AdvertShard[16];

    AdvertConfig(ConfigObj config) {
        this.config = config;
        var shards = config.shards;
        if (shards.isEmpty()) {
            throw new IllegalArgumentException("empty shards");
        }
        var slots = this.slots;
        var defaultShard = shards.get(0);
        for (var i = 1; i < shards.size(); i++) {
            var shard = shards.get(i);
            for (var slot : shard.slots) {
                slots[slot] = shard;
            }
        }
        for (var i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = defaultShard;
            }
        }
    }

    public AdvertShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<AdvertShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<AdvertShard> shards) {
            this.shards = List.copyOf(shards);
        }

    }

    @ToString
    public static final class AdvertShard {

        final int[] slots;
        final IntSet skippedAdvertIds;

        @JsonCreator
        public AdvertShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "skipped-advert-ids", required = false) int[] skippedAdvertIds) {
            this.slots = slots;
            var ids = PrimitiveCollections.emptyIntSet();
            if (skippedAdvertIds != null && skippedAdvertIds.length > 0) {
                ids = PrimitiveCollections.unmodifiableSet(new IntHashSet(skippedAdvertIds));
            }
            this.skippedAdvertIds = ids;
        }

        public boolean skipped(int advertId) {
            return skippedAdvertIds.contains(advertId);
        }

    }

}
