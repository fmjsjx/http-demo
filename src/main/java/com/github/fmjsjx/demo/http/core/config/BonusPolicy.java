package com.github.fmjsjx.demo.http.core.config;

import com.github.fmjsjx.demo.http.api.ItemRange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BonusPolicy {

    public static final BonusPolicy create(int lt, ItemRange bonus) {
        return new BonusPolicy(lt, bonus);
    }

    public static final BonusPolicy of(int lt, ItemRange bonus) {
        return new ImmutableBonusPolicy(lt, bonus);
    }

    private int lt;
    private ItemRange bonus;

    public BonusPolicy toImmutable() {
        return new ImmutableBonusPolicy(lt, bonus);
    }

    private static final class ImmutableBonusPolicy extends BonusPolicy {

        private ImmutableBonusPolicy(int lt, ItemRange bonus) {
            super(lt, bonus);
        }

        @Override
        public void setLt(int lt) {
            // skip
        }

        @Override
        public void setBonus(ItemRange bonus) {
            // skip
        }

        @Override
        public BonusPolicy toImmutable() {
            return this;
        }

    }

}
