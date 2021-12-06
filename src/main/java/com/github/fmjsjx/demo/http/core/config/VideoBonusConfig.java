package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.api.ItemBox;
import com.github.fmjsjx.demo.http.core.config.VideoBonusConfig.VideoBonusShard;
import com.github.fmjsjx.libcommon.collection.CollectorUtil;
import com.github.fmjsjx.libcommon.util.NumberUtil;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import io.netty.util.collection.IntObjectMap;
import lombok.ToString;

@ToString
public class VideoBonusConfig implements ShardingConfig<VideoBonusShard> {

    static final ConfigObj loadConfigFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, ConfigObj.class);
    }

    public static final VideoBonusConfig loadFromYaml(InputStream in) {
        return new VideoBonusConfig(loadConfigFromYaml(in));
    }

    final ConfigObj config;
    @ToString.Exclude
    final VideoBonusShard[] slots = new VideoBonusShard[16];

    VideoBonusConfig(ConfigObj config) {
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

    public VideoBonusShard shard(int slot) {
        return slots[slot];
    }

    @ToString
    public static final class ConfigObj {

        final List<VideoBonusShard> shards;

        @JsonCreator
        public ConfigObj(@JsonProperty(value = "shards", required = true) List<VideoBonusShard> shards) {
            this.shards = List.copyOf(shards);
        }

    }

    @ToString
    public static final class VideoBonusShard {

        final int[] slots;
        final List<VideoConfig> videos;
        @ToString.Exclude
        final IntObjectMap<Optional<VideoConfig>> videoMap;

        @JsonCreator
        public VideoBonusShard(@JsonProperty(value = "slots", required = true) int[] slots,
                @JsonProperty(value = "videos", required = true) List<VideoConfig> videos) {
            this.slots = slots;
            this.videos = List.copyOf(videos);
            this.videoMap = videos.stream().collect(CollectorUtil.toIntObjectMap(VideoConfig::id, Optional::of));
        }

        void validate(int index) {
            // skip
        }

        public Optional<VideoConfig> video(int id) {
            var video = videoMap.get(id);
            return video == null ? Optional.empty() : video;
        }

    }

    @ToString
    public static final class VideoConfig {

        final int id;
        final String name;
        final Optional<String> policy;
        final List<ItemBox> bonus;
        final int sourceId;
        final String remark;
        final OptionalInt limit;
        final OptionalInt dailyLimit;

        @JsonCreator
        public VideoConfig(@JsonProperty(value = "id", required = true) int id,
                @JsonProperty(value = "name", required = true) String name,
                @JsonProperty(value = "policy", required = false) String policy,
                @JsonProperty(value = "bonus", required = false) List<ItemBox> bonus,
                @JsonProperty(value = "source-id", required = false) int sourceId,
                @JsonProperty(value = "remark", required = false) String remark,
                @JsonProperty(value = "limit", required = false) Integer limit,
                @JsonProperty(value = "daily-limit", required = false) Integer dailyLimit) {
            this.id = id;
            this.name = name;
            this.policy = Optional.ofNullable(policy);
            this.bonus = bonus == null ? List.of()
                    : bonus.stream().map(ItemBox::toImmutable).collect(Collectors.toUnmodifiableList());
            this.sourceId = sourceId;
            this.remark = remark;
            this.limit = NumberUtil.optionalInt(limit);
            this.dailyLimit = NumberUtil.optionalInt(dailyLimit);
        }

        public int id() {
            return id;
        }

        public String name() {
            return name;
        }

        public Optional<String> policy() {
            return policy;
        }

        public List<ItemBox> bonus() {
            return bonus;
        }

        public int sourceId() {
            return sourceId;
        }

        public String remark() {
            return remark;
        }

        public OptionalInt limit() {
            return limit;
        }

        public OptionalInt dailyLimit() {
            return dailyLimit;
        }

    }

}
