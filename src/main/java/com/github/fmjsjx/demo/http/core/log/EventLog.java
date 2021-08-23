package com.github.fmjsjx.demo.http.core.log;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.util.LogUtil;
import com.github.fmjsjx.libcommon.json.Jackson2Library;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class EventLog {

    private final long timeMillis = System.currentTimeMillis();

    private int uid;
    private int productId;
    private String channel;
    private int channelId;
    private String clientVersion;
    private String deviceId;
    private int slot;
    private String event;
    private Object data;

    public EventLog(AuthToken token, String event, Object data) {
        this.uid = token.uid();
        this.productId = token.getProductId();
        this.channel = token.getChannel();
        this.channelId = token.getChannelId();
        this.clientVersion = token.getClientVersion();
        this.deviceId = token.getDeviceId();
        this.slot = token.getSlot();
        this.event = event;
        this.data = data;
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
        map.put("_e", event);
        map.put("_d", Jackson2Library.getInstance().dumpsToString(data));
        return map;
    }

}
