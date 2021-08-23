package com.github.fmjsjx.demo.http.core.log.event;

import java.util.List;

import com.github.fmjsjx.demo.http.api.ItemBox;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
abstract class EventData<Self extends EventData<?>> {

    protected List<ItemBox> bonus;
    protected List<ItemBox> cost;

    @SuppressWarnings("unchecked")
    public Self bonus(List<ItemBox> bonus) {
        this.bonus = bonus;
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    public Self cost(List<ItemBox> cost) {
        this.cost = cost;
        return (Self) this;
    }

}
