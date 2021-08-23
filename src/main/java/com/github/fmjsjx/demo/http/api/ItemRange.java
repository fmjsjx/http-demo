package com.github.fmjsjx.demo.http.api;

import com.github.fmjsjx.demo.http.api.Constants.ItemIds;
import com.github.fmjsjx.libcommon.util.RandomUtil;

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
public class ItemRange {

    public static final ItemRange ONE_COIN = ItemRange.of(ItemIds.COIN, 1);

    public static final ItemRange oneCoin() {
        return ONE_COIN;
    }

    public static final ItemRange create(int item, int min, int max) {
        return new ItemRange(item, min, max);
    }

    public static final ItemRange of(int item, int min, int max) {
        if (min == max) {
            return of(item, min);
        }
        return new ImmutableItemRange(item, min, max);
    }

    public static final ItemRange of(int item, int num) {
        return new FixedItemRange(item, num);
    }

    private int item;
    private int min;
    private int max;

    public ItemRange toImmutable() {
        return new ImmutableItemRange(item, min, max);
    }

    public ItemBox toBox() {
        return ItemBox.create(item, randomNum());
    }

    public int randomNum() {
        return RandomUtil.randomInRange(min, max);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemRange) {
            var o = (ItemRange) obj;
            return item == o.item && min == o.min && max == o.max;
        }
        return false;
    }

    private static final class ImmutableItemRange extends ItemRange {

        private ImmutableItemRange(int item, int min, int max) {
            super(item, min, max);
        }

        @Override
        public void setItem(int item) {
            // skip
        }

        @Override
        public void setMin(int min) {
            // skip
        }

        @Override
        public void setMax(int max) {
            // skip
        }

        @Override
        public ItemRange toImmutable() {
            return this;
        }

    }

    private static final class FixedItemRange extends ItemRange {

        private FixedItemRange(int item, int num) {
            super(item, num, num);
        }

        @Override
        public void setItem(int item) {
            // skip
        }

        @Override
        public void setMin(int min) {
            // skip
        }

        @Override
        public void setMax(int max) {
            // skip
        }

        @Override
        public ItemRange toImmutable() {
            return this;
        }

        public ItemBox toBox() {
            return ItemBox.of(getItem(), randomNum());
        }

        public int randomNum() {
            return getMax();
        }

    }

}
