package com.github.fmjsjx.demo.http.util;

import static com.github.fmjsjx.demo.http.api.Constants.ItemIds.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.api.ItemBox;
import com.github.fmjsjx.demo.http.core.config.BonusPolicies;
import com.github.fmjsjx.demo.http.core.log.ItemLog;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.entity.model.Player;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemUtil {

    public static final List<ItemLog> costItems(AuthToken token, Player player, Collection<ItemBox> items, int sourceId,
            String remark) {
        var itemLogs = new ArrayList<ItemLog>(items.size());
        costItems(token, player, items, sourceId, remark, itemLogs);
        return itemLogs;
    }

    public static final int costItems(AuthToken token, Player player, Collection<ItemBox> items, int sourceId,
            String remark, List<ItemLog> itemLogs) {
        for (var item : items) {
            itemLogs.add(costItem(token, player, item, sourceId, remark));
        }
        return items.size();
    }

    public static final ItemLog costItem(AuthToken token, Player player, ItemBox item, int sourceId, String remark) {
        var id = item.getItem();
        var num = Math.abs(item.getNum());
        switch (id) {
        case COIN: {
            var wallet = player.getWallet();
            var original = wallet.getCoin();
            if (original < num) {
                throw ApiErrors.noEnoughCoin();
            }
            wallet.setCoinUsed(wallet.getCoinUsed() + num);
            return token.itemLog(id, original, -num, sourceId, remark);
        }
        case DIAMOND: {
            throw new IllegalArgumentException("diamond cat be cost");
        }
        case RANDOM_COIN:
            throw new IllegalArgumentException("item must be normalized");
        default:
            var items = player.getItems();
            var original = items.get(id).orElse(0);
            if (original < num) {
                throw ApiErrors.noEnoughItem();
            }
            items.put(id, original - num);
            return token.itemLog(id, original, -num, sourceId, remark);
        }
    }

    public static final List<ItemLog> addItems(AuthToken token, Player player, Collection<ItemBox> items, int sourceId,
            String remark) {
        var list = new ArrayList<ItemLog>(items.size());
        addItems(token, player, items, sourceId, remark, list);
        return list;
    }

    public static final int addItems(AuthToken token, Player player, Collection<ItemBox> items, int sourceId,
            String remark, List<ItemLog> itemLogs) {
        for (var item : items) {
            itemLogs.add(addItem(token, player, item, sourceId, remark));
        }
        return items.size();
    }

    public static final ItemLog addItem(AuthToken token, Player player, ItemBox item, int sourceId, String remark) {
        var id = item.getItem();
        var num = Math.abs(item.getNum());
        switch (id) {
        case COIN: {
            var wallet = player.getWallet();
            var daily = player.getDaily();
            var original = wallet.getCoin();
            var coin = Math.min(ConfigUtil.coinLimit(), original + num);
            var add = coin - original;
            wallet.setCoinTotal(wallet.getCoinTotal() + add);
            daily.setCoin(daily.getCoin() + add);
            return token.itemLog(id, original, add, sourceId, remark);
        }
        case DIAMOND: {
            var wallet = player.getWallet();
            var daily = player.getDaily();
            var original = wallet.getDiamond();
            wallet.setDiamond(original + num);
            daily.setDiamond(daily.getDiamond() + num);
            return token.itemLog(id, original, num, sourceId, remark);
        }
        case RANDOM_COIN:
            throw new IllegalArgumentException("item must be normalized");
        default:
            var items = player.getItems();
            var original = items.get(id).orElse(0);
            items.put(id, original + num);
            return token.itemLog(id, original, num, sourceId, remark);
        }
    }

    public static final List<ItemBox> normailze(Player player, Collection<ItemBox> items, BonusPolicies policies) {
        return items.stream().map(item -> normalize(player, item, policies)).collect(Collectors.toList());
    }

    public static final ItemBox normalize(Player player, ItemBox item, BonusPolicies policies) {
        switch (item.getItem()) {
        case RANDOM_COIN:
            var wallet = player.getWallet();
            var bonus = policies.switchBonus(wallet.getCoin());
            var r = bonus.toBox();
            var num = item.getNum();
            if (r.getNum() > num) {
                r = ItemBox.of(COIN, num);
            }
            log.debug("Normalize random coin {} => {} : {} - {}", item, r, wallet, policies);
            return r;
        default:
            return item;
        }
    }

    public static final int totalCoin(List<ItemBox> bonus) {
        return bonus.stream().filter(i -> i.getItem() == COIN).mapToInt(ItemBox::getNum).sum();
    }

    public static final int totalDiamond(List<ItemBox> bonus) {
        return bonus.stream().filter(i -> i.getItem() == DIAMOND).mapToInt(ItemBox::getNum).sum();
    }

    public static final String amountToString(int amount, String unit) {
        switch (amount) {
        case 30:
            return "0.3" + unit;
        case 50:
            return "0.5" + unit;
        case 100:
            return "1" + unit;
        case 200:
            return "2" + unit;
        case 300:
            return "3" + unit;
        case 500:
            return "5" + unit;
        case 1000:
            return "10" + unit;
        default:
            if (amount % 100 == 0) {
                return (amount / 100) + unit;
            } else if (amount % 10 == 0) {
                return String.format("%.1f", amount / 100.0) + unit;
            } else {
                return String.format("%.2f", amount / 100.0) + unit;
            }
        }
    }

    public static final String amountToString(int amount) {
        switch (amount) {
        case 30:
            return "0.3元";
        case 50:
            return "0.5元";
        case 100:
            return "1元";
        case 200:
            return "2元";
        case 300:
            return "3元";
        case 500:
            return "5元";
        case 1000:
            return "10元";
        default:
            if (amount % 100 == 0) {
                return (amount / 100) + "元";
            } else if (amount % 10 == 0) {
                return String.format("%.1f元", amount / 100.0);
            } else {
                return String.format("%.2f元", amount / 100.0);
            }
        }
    }

    private ItemUtil() {
    }

}
