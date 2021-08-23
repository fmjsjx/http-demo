package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.libcommon.util.RandomUtil;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;

@ToString
public class OccurrenceConfig {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final OccurrenceConfig loadFromYaml(InputStream in) {
        return new OccurrenceConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;

    @ToString.Exclude
    final OccurrenceShard[] slots = new OccurrenceShard[16];

    OccurrenceConfig(ConfigObj config) {
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

    public OccurrenceShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<OccurrenceShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<OccurrenceShard> shards) {
            this.shards = shards;
        }

    }

    @ToString
    public static final class OccurrenceShard {

        private static final boolean hit(int rate) {
            if (rate <= 0) {
                return false;
            }
            if (rate >= 100) {
                return true;
            }
            return RandomUtil.randomInt(100) < rate;
        }

        final int[] slots;
        final Map<String, Occurrence> occurences;

        @JsonCreator
        public OccurrenceShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "occurrences", required = true) Map<String, Occurrence> occurences) {
            this.slots = slots;
            this.occurences = Collections.unmodifiableMap(occurences);
        }

        void validate(int index) {
            // skip
        }

        public boolean hit(String key, int value) {
            var o = occurences.get(key);
            if (o == null) {
                return false;
            }
            for (var p : o.policies) {
                if (value < p.lt) {
                    var rate = p.rate;
                    return hit(rate);
                }
            }
            return hit(o.defaultRate);
        }

    }

    @ToString
    public static final class Occurrence {

        final int defaultRate;
        final List<OccurrencePolicy> policies;

        @JsonCreator
        public Occurrence(@JsonProperty(value = "default-rate", required = true) int defaultRate,
                @JsonProperty(value = "policies", required = true) List<OccurrencePolicy> policies) {
            this.defaultRate = defaultRate;
            this.policies = policies;
        }

    }

    @ToString
    public static final class OccurrencePolicy {

        final int lt;
        final int rate;

        @JsonCreator
        public OccurrencePolicy(@JsonProperty(value = "lt", required = true) int lt,
                @JsonProperty(value = "rate", required = true) int rate) {
            this.lt = lt;
            this.rate = rate;
        }

    }

}
