package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.demo.http.core.config.ClientConfig.ClientShard;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.ToString;

@ToString
public class ClientConfig implements ShardingConfig<ClientShard> {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final ClientConfig loadFromYaml(InputStream in) {
        return new ClientConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;

    @ToString.Exclude
    final ClientShard[] slots = new ClientShard[16];

    ClientConfig(ConfigObj config) {
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

    @Override
    public ClientShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<ClientShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<ClientShard> shards) {
            this.shards = shards;
        }

    }

    @ToString
    public static final class ClientShard {

        final int[] slots;
        final JsonNode config;

        @JsonCreator
        public ClientShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "config", required = false) JsonNode config) {
            this.slots = slots;
            this.config = config;
        }

        void validate(int index) {
            // skip
        }

        public JsonNode config() {
            return config;
        }

    }

}
