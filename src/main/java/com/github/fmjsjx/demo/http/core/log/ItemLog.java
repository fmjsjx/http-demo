package com.github.fmjsjx.demo.http.core.log;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.util.LogUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ItemLog {

    private final long timeMillis = System.currentTimeMillis();

    private int uid;
    private int productId;
    private String channel;
    private int channelId;
    private String clientVersion;
    private String deviceId;
    private int slot;
    private int itemId;
    private long original;
    private long number;
    private int sourceId;
    private String remark;

    public ItemLog(AuthToken token, int itemId, long original, long number, int sourceId, String remark) {
        this.uid = token.uid();
        this.productId = token.getProductId();
        this.channel = token.getChannel();
        this.channelId = token.getChannelId();
        this.clientVersion = token.getClientVersion();
        this.deviceId = token.getDeviceId();
        this.slot = token.getSlot();
        this.itemId = itemId;
        this.original = original;
        this.number = number;
        this.sourceId = sourceId;
        this.remark = remark;
    }

    public Map<String, String> toMap() {
        var map = new LinkedHashMap<String, String>();
        map.put("_t", LogUtil.toUnixTime(timeMillis));
        map.put("u", Integer.toString(uid));
        map.put("pi", Integer.toString(productId));
        map.put("c", channel);
        map.put("ci", Integer.toString(channelId));
        map.put("v", clientVersion);
        map.put("d", deviceId);
        map.put("s", Integer.toString(slot));
        map.put("ii", Integer.toString(itemId));
        map.put("o", Long.toString(original));
        map.put("n", Long.toString(number));
        map.put("si", Integer.toString(sourceId));
        map.put("r", remark);
        return map;
    }

}
