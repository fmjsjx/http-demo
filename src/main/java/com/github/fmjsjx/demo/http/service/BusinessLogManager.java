package com.github.fmjsjx.demo.http.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.demo.http.core.log.EventLog;
import com.github.fmjsjx.demo.http.core.log.ItemLog;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.libcommon.json.Jackson2Library;

import io.lettuce.core.RedisFuture;

@Service
public class BusinessLogManager extends RedisWrappedManager {

    private static final Logger eventLogger = LoggerFactory.getLogger("eventLogger");
    private static final Logger itemLogger = LoggerFactory.getLogger("itemLogger");

    public RedisFuture<Long> logEventAsync(EventLog eventLog) {
        var key = "biz:" + eventLog.getProductId() + ":event";
        var value = Jackson2Library.getInstance().dumps(eventLog.toMap());
        logger.debug("[redis:logging] LPUSH {} {}", key, value);
        eventLogger.info("{} - {}", eventLog.getEvent(), value);
        return loggingRedisAsync().lpush(key, value);
    }

    public RedisFuture<Long> logEventAsync(AuthToken token, String event, Object data) {
        return logEventAsync(token.eventLog(event, data));
    }

    public RedisFuture<Long> logItemAsync(ItemLog itemLog) {
        var key = "biz:" + itemLog.getProductId() + ":item";
        var value = Jackson2Library.getInstance().dumps(itemLog.toMap());
        logger.debug("[redis:logging] LPUSH {} {}", key, value);
        itemLogger.info("{}", value);
        return loggingRedisAsync().lpush(key, value);
    }

    public CompletionStage<Long> logItemsAsync(ItemLog... itemLogs) {
        return logItemsAsync(Arrays.asList(itemLogs));
    }

    public CompletionStage<Long> logItemsAsync(List<ItemLog> itemLogs) {
        switch (itemLogs.size()) {
        case 0:
            return CompletableFuture.completedStage(0L);
        case 1:
            return logItemAsync(itemLogs.get(0));
        default:
            var key = "biz:" + itemLogs.get(0).getProductId() + ":item";
            var values = itemLogs.stream().map(o -> Jackson2Library.getInstance().dumps(o.toMap()))
                    .toArray(String[]::new);
            logger.debug("[redis:logging] LPUSH {} {}", key, values);
            for (var value : values) {
                itemLogger.info("{}", value);
            }
            return loggingRedisAsync().lpush(key, values);
        }
    }

}
