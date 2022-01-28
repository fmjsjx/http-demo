package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.core.config.FeaturesConfig.FeaturesShard;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;

@ToString
public class FeaturesConfig implements ShardingConfig<FeaturesShard> {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final FeaturesConfig loadFromYaml(InputStream in) {
        return new FeaturesConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;
    @ToString.Exclude
    final FeaturesShard[] slots = new FeaturesShard[16];

    FeaturesConfig(ConfigObj config) {
        this.config = config;
        var shards = config.shards;
        if (shards.isEmpty()) {
            throw new IllegalArgumentException("empty shards");
        }
        var slots = this.slots;
        var defaultShard = shards.get(0);
        defaultShard.validate(0);
        for (var i = 1; i < shards.size(); i++) {
            var shard = shards.get(i);
            shard.validate(i);
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

    public FeaturesShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<FeaturesShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<FeaturesShard> shards) {
            this.shards = List.copyOf(shards);
        }

    }

    @ToString
    public static final class FeaturesShard {

        final int[] slots;
        final List<String> commonFeatures;
        final Map<String, FilterResult> filters;

        @JsonCreator
        public FeaturesShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "common-features", required = false) List<String> commonFeatures,
                @JsonProperty(value = "filters", required = false) Map<String, String> filters) {
            this.slots = slots;
            this.commonFeatures = commonFeatures == null ? List.of() : List.copyOf(commonFeatures);
            this.filters = filters == null ? Map.of()
                    : filters.entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey,
                            e -> FilterResult.valueOf(e.getValue().toUpperCase())));
        }

        void validate(int index) {
            // skip
        }
        
        public List<String> commonFeatures() {
            return commonFeatures;
        }

        public FilterResult filter(String feature) {
            return filters.getOrDefault(feature, FilterResult.NEUTRAL);
        }

        public boolean allowRookie(String feature) {
            return filter(feature) != FilterResult.DENY;
        }

        public boolean allowAll(String feature) {
            return filter(feature) == FilterResult.ACCEPT;
        }

    }

    public enum FilterResult {

        ACCEPT, NEUTRAL, DENY;

    }

}
