package com.github.fmjsjx.demo.http.server;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.PATCH;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.fmjsjx.demo.http.ServerProperties;
import com.github.fmjsjx.demo.http.service.ConfigManager;
import com.github.fmjsjx.libcommon.util.NumberUtil;
import com.github.fmjsjx.libcommon.util.RuntimeUtil;
import com.github.fmjsjx.libnetty.handler.ssl.SslContextProvider;
import com.github.fmjsjx.libnetty.http.HttpContentCompressorFactory;
import com.github.fmjsjx.libnetty.http.server.DefaultHttpServer;
import com.github.fmjsjx.libnetty.http.server.HttpServer;
import com.github.fmjsjx.libnetty.http.server.component.WorkerPool;
import com.github.fmjsjx.libnetty.http.server.middleware.AccessLogger;
import com.github.fmjsjx.libnetty.http.server.middleware.AccessLogger.Slf4jLoggerWrapper;
import com.github.fmjsjx.libnetty.http.server.middleware.PathFilterMiddleware;
import com.github.fmjsjx.libnetty.http.server.middleware.Router;
import com.github.fmjsjx.libnetty.transport.TransportLibrary;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Servers implements InitializingBean, DisposableBean {

    private static final CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
            .allowedRequestMethods(GET, POST, PUT, PATCH, DELETE).allowedRequestHeaders("*").allowNullOrigin().build();

    @Autowired
    private ConfigManager configManager;
    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private Router router;
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private TokenVerifier tokenVerifier;
    @Autowired
    private RouteErrorHandler apiErrorHandler;

    private volatile HttpServer httpServer;

    private volatile EventLoopGroup httpBossBroup;
    private volatile EventLoopGroup workerGroup;

    @Override
    public synchronized void afterPropertiesSet() throws Exception {
        var ssl = serverProperties.getSsl();
        var sslEnabled = ssl != null && ssl.isEnabled();
        var sslContextProvider = configManager.sslContextProvider();
        var transportLibrary = TransportLibrary.getDefault();
        var ioThreads = NumberUtil.intValue(serverProperties.getIoThreads(), RuntimeUtil.availableProcessors());
        workerGroup = transportLibrary.createGroup(ioThreads, new DefaultThreadFactory("io-worker"));
        startupHttpServer(sslEnabled, sslContextProvider, transportLibrary);
    }

    private void startupHttpServer(boolean sslEnabled, SslContextProvider sslContextProvider,
            TransportLibrary transportLibrary) throws Exception {
        httpBossBroup = transportLibrary.createGroup(1, new DefaultThreadFactory("http-boss"));
        var httpProperties = serverProperties.getHttp();
        var server = new DefaultHttpServer("http", httpProperties.getPort());
        if (sslEnabled) {
            server.enableSsl(sslContextProvider);
        }
        server.transport(httpBossBroup, workerGroup, transportLibrary.serverChannelClass()).corsConfig(corsConfig)
                .soBackLog(1024).supportJson().component(workerPool).component(apiErrorHandler)
                .applyCompressionSettings(HttpContentCompressorFactory.defaultSettings());
        server.defaultHandlerProvider().addLast(new AccessLogger(new Slf4jLoggerWrapper("accessLogger"),
                ":method :path :http-version :remote-addr - :status :response-time ms :result-length-humanreadable"))
                .addLast(PathFilterMiddleware.toFilter("/api/partners", "/api/auth").negate(), tokenVerifier)
                // may add token validation middle-ware here
                .addLast(router);
        this.httpServer = server;
        server.startup();
        log.info("Server {} started.", server);
    }

    @Override
    public void destroy() throws Exception {
        var httpServer = this.httpServer;
        if (httpServer != null && httpServer.isRunning()) {
            httpServer.shutdown();
            log.info("Sever {} stopped.", httpServer);
        }
        var httpBossBroup = this.httpBossBroup;
        if (httpBossBroup != null && !httpBossBroup.isShuttingDown()) {
            httpBossBroup.shutdownGracefully();
        }
        var workerGroup = this.workerGroup;
        if (workerGroup != null && !workerGroup.isShuttingDown()) {
            workerGroup.shutdownGracefully();
        }
    }

}
