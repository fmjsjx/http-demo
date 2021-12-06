package com.github.fmjsjx.demo.http.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.fmjsjx.demo.http.ServerProperties;
import com.github.fmjsjx.demo.http.core.config.AdvertConfig;
import com.github.fmjsjx.demo.http.core.config.BonusPoliciesConfig;
import com.github.fmjsjx.demo.http.core.config.ErrorMessageConfig;
import com.github.fmjsjx.demo.http.core.config.OccurrenceConfig;
import com.github.fmjsjx.demo.http.core.config.PlayerInitConfig;
import com.github.fmjsjx.demo.http.core.config.ServerConfig;
import com.github.fmjsjx.demo.http.core.config.VideoBonusConfig;
import com.github.fmjsjx.demo.http.core.config.AdvertConfig.AdvertShard;
import com.github.fmjsjx.demo.http.core.config.BonusPoliciesConfig.BonusPoliciesShard;
import com.github.fmjsjx.demo.http.core.config.OccurrenceConfig.OccurrenceShard;
import com.github.fmjsjx.demo.http.core.config.PlayerInitConfig.PlayerInitShard;
import com.github.fmjsjx.demo.http.core.config.VideoBonusConfig.VideoBonusShard;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.util.ConfigUtil;
import com.github.fmjsjx.libcommon.util.StringUtil;
import com.github.fmjsjx.libnetty.handler.ssl.PermutableSslContextProvider;
import com.github.fmjsjx.libnetty.handler.ssl.SslContextProviders;

import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConfigManager implements InitializingBean, DisposableBean {

    private volatile PermutableSslContextProvider sslContextProvider;

    private static final String SERVER = "server.yml";
    private static final String ERROR_MESSAGE = "error-message.yml";

    private static final String BONUS_POLICIES = "bonus-policies.yml";
    private static final String VIDEO_BONUS = "video-bonus.yml";
    private static final String OCCURRENCE = "occurrence.yml";
    private static final String PLAYER_INIT = "player-init.yml";
    private static final String ADVERT = "advert.yml";

    private final ConcurrentMap<String, FileInfo> fileInfos = new ConcurrentHashMap<>();

    private final AtomicReference<BonusPoliciesConfig> bonusPoliciesRef = new AtomicReference<>();
    private final AtomicReference<VideoBonusConfig> videoBonusRef = new AtomicReference<>();
    private final AtomicReference<OccurrenceConfig> occurrenceRef = new AtomicReference<>();
    private final AtomicReference<PlayerInitConfig> playerInitRef = new AtomicReference<>();
    private final AtomicReference<AdvertConfig> advertRef = new AtomicReference<>();

    private WatchService watchService;
    private WatchKey sslKey;

    private final Map<String, Runnable> reloadFunctions = new HashMap<>();
    private final ExecutorService executor = Executors
            .newSingleThreadExecutor(new DefaultThreadFactory("watch-config", true));

    @Autowired
    private ServerProperties serverProperties;

    @Override
    public synchronized void afterPropertiesSet() throws Exception {
        initReloadFunctions();

        var dir = Paths.get(ConfigUtil.confDir());

        var watchService = dir.getFileSystem().newWatchService();
        this.watchService = watchService;
        dir.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
        var ssl = serverProperties.getSsl();
        if (ssl != null && ssl.isEnabled()) {
            sslKey = dir.resolve("ssl").register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
        }
        executor.execute(new WatchingTask());
        log.info("Start watch-config task.");
    }

    private void initReloadFunctions() throws Exception {
        var ssl = serverProperties.getSsl();
        if (ssl != null && ssl.isEnabled()) {
            var fileName = ssl.getKeyCertChainFile();
            initReloadFunction(fileName, this::loadSsl);
        }

        initReloadFunction(SERVER, this::loadServerConfig);
        initReloadFunction(ERROR_MESSAGE, this::loadErrorMessage);

        initReloadFunction(BONUS_POLICIES, this::loadBonusPolicies);
        initReloadFunction(VIDEO_BONUS, this::loadVideoBonus);
        initReloadFunction(OCCURRENCE, this::loadOccurrence);
        initReloadFunction(PLAYER_INIT, this::loadPlayerInit);
        initReloadFunction(ADVERT, this::loadAdvert);
    }

    private void initReloadFunction(String fileName, LoadAction action) throws Exception {
        action.load();
        reloadFunctions.put(fileName, reloadAction(fileName, action));
    }

    @Override
    public synchronized void destroy() throws Exception {
        log.info("Shutdown watch-config thread executor.");
        executor.shutdown();
        var watchService = this.watchService;
        if (watchService != null) {
            log.info("Close watch service for configuration.");
            watchService.close();
        }
    }

    private void loadSsl() throws Exception {
        var ssl = serverProperties.getSsl();
        var keyCertChainFile = Paths.get(ConfigUtil.confDir(), ssl.getKeyCertChainFile()).toFile();
        var keyFile = Paths.get(ConfigUtil.confDir(), ssl.getKeyFile()).toFile();
        var keyPassword = ssl.getKeyPassword();
        SslContextBuilder sslContextBuilder;
        if (StringUtil.isNotBlank(keyPassword)) {
            sslContextBuilder = SslContextBuilder.forServer(keyCertChainFile, keyFile, keyPassword);
        } else {
            sslContextBuilder = SslContextBuilder.forServer(keyCertChainFile, keyFile);
        }
        try {
            if (OpenSsl.isAvailable()) {
                sslContextBuilder.sslProvider(SslProvider.OPENSSL);
            }
        } catch (Exception e) {
            // ignore
        }
        var sslContext = sslContextBuilder.build();
        var sslContextProvider = this.sslContextProvider;
        if (sslContextProvider == null) {
            synchronized (this) {
                sslContextProvider = this.sslContextProvider;
                if (sslContextProvider == null) {
                    this.sslContextProvider = sslContextProvider = SslContextProviders.permutable(sslContext);
                    return;
                }
            }
        }
        sslContextProvider.set(sslContext);
    }

    private void loadServerConfig() throws Exception {
        var file = new File(ConfigUtil.confDir(), SERVER);
        if (file.exists()) {
            loadConfigFromFile(SERVER, ServerConfig::loadFromYaml, ServerConfig::set);
        } else {
            ServerConfig.resetToDefault();
            log.warn("Missing configuration {}, use default: {}", SERVER, ServerConfig.getInstance());
        }
    }

    private void loadErrorMessage() throws Exception {
        loadConfigFromFile(ERROR_MESSAGE, ErrorMessageConfig::loadFromYaml, ErrorMessageConfig::set);
    }

    private void loadBonusPolicies() throws Exception {
        loadConfigFromFile(BONUS_POLICIES, BonusPoliciesConfig::loadFromYaml, bonusPoliciesRef::set);
    }

    private void loadVideoBonus() throws Exception {
        loadConfigFromFile(VIDEO_BONUS, VideoBonusConfig::loadFromYaml, videoBonusRef::set);
    }

    private void loadOccurrence() throws Exception {
        loadConfigFromFile(OCCURRENCE, OccurrenceConfig::loadFromYaml, occurrenceRef::set);
    }

    private void loadPlayerInit() throws Exception {
        loadConfigFromFile(PLAYER_INIT, PlayerInitConfig::loadFromYaml, playerInitRef::set);
    }

    private void loadAdvert() throws Exception {
        loadConfigFromFile(ADVERT, AdvertConfig::loadFromYaml, advertRef::set);
    }

    public BonusPoliciesShard bonusPoliciesShard(AuthToken token) {
        return bonusPoliciesRef.get().shard(token.getSlot());
    }

    public VideoBonusShard videoBonusShard(AuthToken token) {
        return videoBonusRef.get().shard(token.getSlot());
    }

    public OccurrenceShard occurrenceShard(AuthToken token) {
        return occurrenceRef.get().shard(token.getSlot());
    }

    public PlayerInitShard playerInitShard(AuthToken token) {
        return playerInitRef.get().shard(token.getSlot());
    }

    public AdvertShard advertShard(AuthToken token) {
        return advertRef.get().shard(token.getSlot());
    }

    public PermutableSslContextProvider sslContextProvider() {
        return sslContextProvider;
    }

    private <CFG> void loadConfigFromFile(String filename, Function<InputStream, CFG> loader, Consumer<CFG> setter)
            throws Exception {
        var fileInfos = this.fileInfos;
        var file = new File(ConfigUtil.confDir(), filename);
        var fileInfo = fileInfos.get(filename);
        var lastModified = file.lastModified();
        var length = file.length();
        if (fileInfo != null && fileInfo.isSame(lastModified, length)) {
            log.debug("Break duplicated event.");
            return;
        }
        try (var in = new BufferedInputStream(new FileInputStream(file))) {
            var config = loader.apply(in);
            log.debug("Loaded configuration {}: {}", filename, config);
            setter.accept(config);
        }
        fileInfos.put(filename, new FileInfo(lastModified, length));
    }

    private Runnable reloadAction(String fileName, LoadAction action) {
        return () -> {
            log.info("Re-loading configuration {}", fileName);
            try {
                action.load();
            } catch (Exception e) {
                log.error("Unexcepted error occurs when load {}", fileName, e);
            }
        };
    }

    private void execute(Runnable action) {
        var executor = this.executor;
        if (!executor.isShutdown()) {
            executor.execute(action);
        }
    }

    private void runTask() throws InterruptedException, ClosedWatchServiceException {
        var watchKey = watchService.poll(30, TimeUnit.SECONDS);
        if (watchKey != null) {
            try {
                boolean isSsl = watchKey.equals(sslKey);
                var events = watchKey.pollEvents();
                if (events.size() > 0) {
                    for (var watchEvent : events) {
                        var fileName = watchEvent.context().toString();
                        if (isSsl) {
                            fileName = "ssl/" + fileName;
                        }
                        log.debug("Polled event {} {}", watchEvent.kind(), fileName);
                        if (watchEvent.count() == 1) {
                            var function = reloadFunctions.get(fileName);
                            if (function != null) {
                                execute(function);
                            } else {
                                log.debug("Skip unknown file: {}", fileName);
                            }
                        }
                    }
                }
            } finally {
                watchKey.reset();
            }
        }
    }

    private final class WatchingTask implements Runnable {

        @Override
        public void run() {
            try {
                runTask();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                // ignore
            } catch (Throwable e) {
                log.error("Unexpected error occurs when watching configuration", e);
            } finally {
                execute(this);
            }
        }

    }

    @RequiredArgsConstructor
    private static final class FileInfo {

        private final long lastModified;
        private final long length;

        private boolean isSame(long lastModified, long length) {
            return this.lastModified == lastModified && this.length == length;
        }

    }

    @FunctionalInterface
    private interface LoadAction {

        void load() throws Exception;

    }

}
