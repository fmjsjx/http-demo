package com.github.fmjsjx.demo.http.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class ItemsResult<Self extends ItemsResult<?>> {

    private List<ItemBox> bonus;
    private List<ItemBox> cost;

    @SuppressWarnings("unchecked")
    public Self bonus(List<ItemBox> bonus) {
        setBonus(bonus);
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    public Self cost(List<ItemBox> cost) {
        setCost(cost);
        return (Self) this;
    }

}
