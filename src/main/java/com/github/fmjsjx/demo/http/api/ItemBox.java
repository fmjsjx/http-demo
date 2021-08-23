package com.github.fmjsjx.demo.http.api;

import com.github.fmjsjx.demo.http.api.Constants.ItemIds;

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
public class ItemBox {

    public static final ItemBox ONE_COIN = ItemBox.of(ItemIds.COIN, 1);

    public static final ItemBox create(int item, int num) {
        return new ItemBox(item, num);
    }

    public static final ItemBox of(int item, int num) {
        return new ImmutableItemBox(item, num);
    }

    protected int item;
    protected int num;

    public ItemBox times(int times) {
        if (times != 1) {
            num *= times;
        }
        return this;
    }

    public ItemBox toImmutable() {
        return new ImmutableItemBox(item, num);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemBox) {
            var o = (ItemBox) obj;
            return item == o.item && num == o.num;
        }
        return false;
    }

    private static final class ImmutableItemBox extends ItemBox {

        private ImmutableItemBox(int item, int num) {
            super(item, num);
        }

        @Override
        public void setItem(int item) {
            // skip
        }

        @Override
        public void setNum(int num) {
            // skip
        }

        public ItemBox times(int times) {
            if (times != 1) {
                return new ImmutableItemBox(item, num * times);
            }
            return this;
        }

        @Override
        public ItemBox toImmutable() {
            return this;
        }

    }

}
