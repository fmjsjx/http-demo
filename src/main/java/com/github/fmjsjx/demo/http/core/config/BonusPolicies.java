package com.github.fmjsjx.demo.http.core.config;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.demo.http.api.ItemRange;

import lombok.ToString;

@ToString
public class BonusPolicies {

    final List<BonusPolicy> policies;
    final ItemRange defaultBonus;

    @JsonCreator
    public BonusPolicies(@JsonProperty(value = "policies", required = true) List<BonusPolicy> policies,
            @JsonProperty(value = "default-bonus", required = true) ItemRange defaultBonus) {
        this.policies = policies.stream().map(BonusPolicy::toImmutable).collect(Collectors.toUnmodifiableList());
        this.defaultBonus = ItemRange.oneCoin().equals(defaultBonus) ? ItemRange.oneCoin() : defaultBonus.toImmutable();
    }

    public List<BonusPolicy> policies() {
        return policies;
    }

    public ItemRange defaultBonus() {
        return defaultBonus;
    }

    public ItemRange switchBonus(long value) {
        for (var policy : policies) {
            if (value < policy.getLt()) {
                return policy.getBonus();
            }
        }
        return defaultBonus;
    }

}
