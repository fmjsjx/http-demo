package com.github.fmjsjx.demo.http.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;

import com.github.fmjsjx.demo.http.entity.model.Player;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class ResultData {

    private static final OptionalInt FORCE = OptionalInt.of(1);

    public static final ResultData create() {
        return new ResultData();
    }

    public static final ResultData create(Object result) {
        return create().result(result);
    }

    public static final ResultData of(Object result, Player player, int retryCount) {
        return create(result).fix(player, retryCount);
    }

    public static final ResultData of(Player player, int retryCount) {
        return create().fix(player, retryCount);
    }

    private Object result;
    private Object sync;
    private OptionalInt force = OptionalInt.empty();
    private Object del;
    private List<String> events;

    public ResultData result(Object result) {
        this.result = result;
        return this;
    }

    public ResultData fix(Player player, int retryCount) {
        if (retryCount > 0) {
            return sync(player).force();
        }
        if (player.updated()) {
            sync(player.toUpdate());
            var deleteMap = player.toDelete();
            if (!deleteMap.isEmpty()) {
                del(deleteMap);
            }
        }
        return this;
    }

    public ResultData sync(Object sync) {
        this.sync = sync;
        return this;
    }

    public ResultData force() {
        this.force = FORCE;
        return this;
    }

    public ResultData unforce() {
        this.force = OptionalInt.empty();
        return this;
    }

    public ResultData force(boolean force) {
        return force ? force() : unforce();
    }

    public ResultData del(Object del) {
        this.del = del;
        return this;
    }

    public ResultData events(List<String> events) {
        this.events = events;
        return this;
    }

    public ResultData event(String event) {
        var events = this.events;
        if (events == null) {
            this.events = events = new ArrayList<>();
        }
        events.add(event);
        return this;
    }

    public ResultData appendEvents(Collection<String> events) {
        if (this.events == null) {
            this.events = new ArrayList<>(events);
        } else {
            this.events.addAll(events);
        }
        return this;
    }

}
