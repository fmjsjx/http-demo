package com.github.fmjsjx.demo.http.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.github.fmjsjx.demo.http.api.ResultData;
import com.github.fmjsjx.demo.http.core.log.ItemLog;
import com.github.fmjsjx.demo.http.entity.model.Player;

import lombok.ToString;

@ToString
public class ServiceContext {

    static final ServiceContext create(AuthToken token, LocalDateTime time) {
        return new ServiceContext(token, time, new ArrayList<>(), new ArrayList<>());
    }

    static final ServiceContext create(AuthToken token) {
        return create(token, LocalDateTime.now());
    }

    static final ServiceContext create(AuthToken token, Player player) {
        return create(token).player(player);
    }

    static final ServiceContext create(AuthToken token, Player player, LocalDateTime time) {
        return create(token, time).player(player);
    }

    private final AuthToken token;
    private Player player;
    private final LocalDateTime time;
    private final List<String> events;
    private final List<ItemLog> itemLogs;

    private ServiceContext(AuthToken token, LocalDateTime time, List<String> events, List<ItemLog> itemLogs) {
        this.token = token;
        this.time = time;
        this.events = events;
        this.itemLogs = itemLogs;
    }

    public AuthToken token() {
        return token;
    }

    public Player player() {
        return player;
    }

    public ServiceContext player(Player player) {
        this.player = player;
        return this;
    }

    public LocalDateTime time() {
        return time;
    }

    public List<String> events() {
        return events;
    }

    public boolean hasEvent() {
        return events.size() > 0;
    }

    public boolean hasEvent(String event) {
        var events = this.events;
        return events.size() > 0 && events.contains(event);
    }

    public ServiceContext event(String event) {
        var events = this.events;
        if (!events.contains(event)) {
            events.add(event);
        }
        return this;
    }

    public List<ItemLog> itemLogs() {
        return itemLogs;
    }

    public boolean hasItemLog() {
        return itemLogs.size() > 0;
    }

    public ServiceContext itemLog(ItemLog itemLog) {
        itemLogs.add(itemLog);
        return this;
    }

    public ResultData toResultData(int retryCount) {
        return ResultData.of(player, retryCount).events(this);
    }

    public ResultData toResultData(Object result, int retryCount) {
        return ResultData.of(result, player, retryCount).events(this);
    }

}
