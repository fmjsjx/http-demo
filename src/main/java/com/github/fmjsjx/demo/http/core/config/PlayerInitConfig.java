package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.api.ItemBox;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;

@ToString
public class PlayerInitConfig {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final PlayerInitConfig loadFromYaml(InputStream in) {
        return new PlayerInitConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;
    @ToString.Exclude
    final PlayerInitShard[] slots = new PlayerInitShard[16];

    PlayerInitConfig(ConfigObj config) {
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

    public PlayerInitShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<PlayerInitShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<PlayerInitShard> shards) {
            this.shards = List.copyOf(shards);
        }

    }

    @ToString
    public static final class PlayerInitShard {

        final int[] slots;
        final int coin;
        final int diamond;
        final int rope;
        final List<ItemBox> items;

        @JsonCreator
        public PlayerInitShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "coin", required = false) int coin,
                @JsonProperty(value = "diamond", required = false) int diamond,
                @JsonProperty(value = "rope", required = false) int rope,
                @JsonProperty(value = "items", required = false) List<ItemBox> items) {
            this.slots = slots;
            this.coin = coin;
            this.diamond = diamond;
            this.rope = rope;
            this.items = items == null ? List.of()
                    : items.stream().map(ItemBox::toImmutable).collect(Collectors.toUnmodifiableList());
        }

        public int coin() {
            return coin;
        }

        public int diamond() {
            return diamond;
        }

        public int rope() {
            return rope;
        }

        public List<ItemBox> items() {
            return items;
        }

    }

}
