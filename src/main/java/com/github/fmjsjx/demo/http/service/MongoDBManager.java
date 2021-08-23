package com.github.fmjsjx.demo.http.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.libcommon.util.RandomUtil;
import com.mongodb.client.MongoDatabase;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MongoDBManager implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    private final IntObjectMap<MongoDatabase> gameDatabaseMap = new IntObjectHashMap<>();
    private int[] groupIds;

    private static final int parseGroupId(String beanName) {
        var pattern = Pattern.compile("game(\\d+)MongoDatabase");
        var matcher = pattern.matcher(beanName);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        var dbMap = applicationContext.getBeansOfType(MongoDatabase.class);
        List<Integer> groupIds = new ArrayList<>();
        dbMap.forEach((beanName, bean) -> {
            var groupId = parseGroupId(beanName);
            if (groupId > 0) {
                gameDatabaseMap.put(groupId, bean);
                groupIds.add(groupId);
            }
        });
        this.groupIds = groupIds.stream().mapToInt(Integer::intValue).toArray();
    }

    public int randomGroupId() {
        return RandomUtil.randomOne(groupIds);
    }

    public MongoDatabase gameDatabase(int groupId) {
        var db = gameDatabaseMap.get(groupId);
        if (log.isDebugEnabled()) {
            log.debug("[mongodb:group] Choose group {}: {}", groupId, db);
        }
        return db;
    }

}
