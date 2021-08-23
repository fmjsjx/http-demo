package com.github.fmjsjx.demo.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.fmjsjx.demo.http.ServerProperties.HTTPClientMode;
import com.github.fmjsjx.libcommon.util.RuntimeUtil;
import com.github.fmjsjx.libnetty.http.client.DefaultHttpClient;
import com.github.fmjsjx.libnetty.http.client.HttpClient;
import com.github.fmjsjx.libnetty.http.client.SimpleHttpClient;
import com.github.fmjsjx.libnetty.http.server.component.WorkerPool;
import com.github.fmjsjx.libnetty.http.server.component.WrappedWorkerPool;

import io.netty.util.concurrent.DefaultThreadFactory;

@Configuration
@EnableConfigurationProperties({ AppProperties.class, ServerProperties.class, WeChatProperties.class })
@MapperScans({ @MapperScan(basePackages = "com.douzi.games.cowboy.dao") })
public class CoreConfig {

    @Bean(name = "workerExecutor", destroyMethod = "shutdown")
    public ExecutorService workerExecutor() {
        // core pool size = CPU
        var corePoolSize = Math.max(1, RuntimeUtil.availableProcessors());
        // maximum pool size = CPU * 16
        var maximumPoolSize = RuntimeUtil.availableProcessors() * 16;
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("worker"));
    }

    @Bean(name = "workerPool", destroyMethod = "shutdown")
    public WorkerPool workerPool(ExecutorService workerExecutor) {
        return new WrappedWorkerPool(workerExecutor, false);
    }

    @Bean(name = "globalScheduledExecutor", destroyMethod = "shutdown")
    public ScheduledExecutorService globalScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("scheduler"));
    }

    @Bean(name = "globalHttpClient")
    public HttpClient globalHttpClient(ServerProperties properties) {
        if (properties.getHttpClientMode() == HTTPClientMode.SIMPLE) {
            return SimpleHttpClient.builder().enableCompression().build();
        }
        return DefaultHttpClient.builder().enableCompression().build();
    }

}
