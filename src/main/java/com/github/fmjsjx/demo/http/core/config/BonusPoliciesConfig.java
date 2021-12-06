package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.core.config.BonusPoliciesConfig.BonusPoliciesShard;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;
import lombok.ToString.Exclude;

@ToString
public class BonusPoliciesConfig implements ShardingConfig<BonusPoliciesShard> {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final BonusPoliciesConfig loadFromYaml(InputStream in) {
        return new BonusPoliciesConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;
    @Exclude
    final BonusPoliciesShard[] slots = new BonusPoliciesShard[16];

    public BonusPoliciesConfig(ConfigObj config) {
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

    public BonusPoliciesShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<BonusPoliciesShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<BonusPoliciesShard> shards) {
            this.shards = List.copyOf(shards);
        }

    }

    @ToString
    public static final class BonusPoliciesShard {

        final int[] slots;
        final Map<String, BonusPolicies> bonusPolicies;
        final BonusPolicies globalBonus;

        @JsonCreator
        public BonusPoliciesShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "policies", required = true) Map<String, BonusPolicies> bonusPolicies) {
            this.slots = slots;
            this.bonusPolicies = Map.copyOf(bonusPolicies);
            this.globalBonus = bonusPolicies.get("global");
        }

        public BonusPolicies globalPolicies() {
            return globalBonus;
        }

        public BonusPolicies policies(String key) {
            return bonusPolicies.getOrDefault(key, globalBonus);
        }

    }
}
