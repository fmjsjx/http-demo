package com.github.fmjsjx.demo.http.sdk.wechat;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_XML;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.fmjsjx.demo.http.WeChatAppProperties;
import com.github.fmjsjx.demo.http.WeChatProperties;
import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.util.XmlUtil;
import com.github.fmjsjx.libcommon.json.JsoniterLibrary;
import com.github.fmjsjx.libcommon.util.RandomUtil;
import com.github.fmjsjx.libnetty.handler.ssl.SslContextProviders;
import com.github.fmjsjx.libnetty.http.client.HttpClient;
import com.github.fmjsjx.libnetty.http.client.HttpClient.Response;
import com.github.fmjsjx.libnetty.http.client.HttpContentHandlers;
import com.github.fmjsjx.libnetty.http.client.HttpContentHolders;
import com.github.fmjsjx.libnetty.http.client.SimpleHttpClient;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WeChatSdkClient implements InitializingBean, DisposableBean {

    private static final Logger sdkWechatLogger = LoggerFactory.getLogger("sdkWechatLogger");

    private static final URI WECHAT_TRANSFERS_URI = URI
            .create("https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers");

    @Autowired
    @Qualifier("globalHttpClient")
    private HttpClient globalHttpClient;
    @Autowired
    private WeChatProperties weChatProperties;

    private HttpClient globalWechatMchHttpClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initialize global WeChat Payment API SDK");
        try (var keyCertChainInputStream = getClass().getResourceAsStream("/wechat/apiclient_cert.pem");
                var keyInputStream = getClass().getResourceAsStream("/wechat/apiclient_key_pkcs8.pem")) {
            var sslContext = SslContextBuilder.forClient().keyManager(keyCertChainInputStream, keyInputStream).build();
            globalWechatMchHttpClient = SimpleHttpClient.builder().ioThreads(1)
                    .sslContextProvider(SslContextProviders.simple(sslContext)).enableCompression().build();
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("Close wechat pay thread executor.");
        if (globalWechatMchHttpClient != null) {
            log.info("Close global wechat pay HTTP client.");
            globalWechatMchHttpClient.close();
        }
    }

    public CompletableFuture<Response<String>> httpGetAsync(URI uri) {
        return globalHttpClient.request(uri).get().sendAsync(HttpContentHandlers.ofString());
    }

    public AccessTokenResponse getAccessToken(WeChatAppProperties wechatApp, String code) {
        var qse = new QueryStringEncoder("https://api.weixin.qq.com/sns/oauth2/access_token");
        qse.addParam("appid", wechatApp.getAppid());
        qse.addParam("secret", wechatApp.getSecret());
        qse.addParam("code", code);
        qse.addParam("grant_type", "authorization_code");
        var uri = URI.create(qse.toString());
        log.debug("[WeChat] get access token, uri = {}", uri);
        var sendTime = System.currentTimeMillis();
        try {
            var resp = globalHttpClient.request(uri).get().sendAsync(HttpContentHandlers.ofString())
                    .orTimeout(60, TimeUnit.SECONDS).get();
            var millis = System.currentTimeMillis() - sendTime;
            sdkWechatLogger.info("Get Access Token: {} <<< {} {} ms - {}", uri, resp.status(), millis, resp.content());
            if (resp.statusCode() != 200) {
                throw ApiErrors.partnerAuthFailure();
            }
            var body = resp.content();
            log.debug("[WeChat] get access token response: {}", body);
            var result = JsoniterLibrary.defaultInstance().loads(body, AccessTokenResponse.class);
            if (result.getErrcode() != 0) {
                log.warn("[Wechat] get access token failed: {}", body);
                throw ApiErrors.partnerAuthFailure();
            }
            return result;
        } catch (Exception e) {
            var millis = System.currentTimeMillis() - sendTime;
            sdkWechatLogger.error("Get Access Token Failed: {} <<< {} ms", uri, millis, e);
            throw ApiErrors.partnerServiceError(e);
        }
    }

    public AccessTokenResponse getAccessToken(String code) {
        return getAccessToken(weChatProperties.getGlobal(), code);
    }

    public UserInfoResponse getUserInfoAsync(AccessTokenResponse accessTokenResponse) {
        return getUserInfoAsync(accessTokenResponse.getAccessToken(), accessTokenResponse.getOpenid());
    }

    public UserInfoResponse getUserInfoAsync(String accessToken, String openid) {
        var qse = new QueryStringEncoder("https://api.weixin.qq.com/sns/userinfo");
        qse.addParam("access_token", accessToken);
        qse.addParam("openid", openid);
        var uri = URI.create(qse.toString());
        log.debug("[WeChat] get userinfo, uri = {}", uri);
        var sendTime = System.currentTimeMillis();
        try {
            var resp = globalHttpClient.request(uri).get().sendAsync(HttpContentHandlers.ofString())
                    .orTimeout(60, TimeUnit.SECONDS).get();
            var millis = System.currentTimeMillis() - sendTime;
            sdkWechatLogger.info("Get UserInfo: {} <<< {} {} ms - {}", uri, resp.status(), millis, resp.content());
            if (resp.statusCode() != 200) {
                throw ApiErrors.partnerAuthFailure();
            }
            var body = resp.content();
            log.debug("[WeChat] get userinfo response: {}", body);
            var result = JsoniterLibrary.defaultInstance().loads(body, UserInfoResponse.class);
            if (result.getErrcode() != 0) {
                log.warn("[Wechat] get userinfo failed: {}", body);
                throw ApiErrors.partnerAuthFailure();
            }
            return result;
        } catch (Exception e) {
            var millis = System.currentTimeMillis() - sendTime;
            sdkWechatLogger.error("Get UserInfo Failed: {} <<< {} ms - {}", uri, millis, e);
            throw ApiErrors.partnerServiceError(e);
        }
    }

    public TransferResponse postPayTransfer(AuthToken token, String tradeNo, int amount) throws Exception {
        return postPayTransfer(weChatProperties.getGlobal(), token, tradeNo, amount);
    }

    public TransferResponse postPayTransfer(WeChatAppProperties weChatApp, AuthToken token, String tradeNo, int amount)
            throws Exception {
        TransferResponse result = null;
        for (int retryCount = 0; retryCount <= 3; retryCount++) {
            result = postPayTransfer(weChatApp, token, tradeNo, amount, retryCount);
            if (result.resultSuccess()) {
                return result;
            }
            switch (result.getErrCode()) {
            case MchResponse.FREQ_LIMIT:
            case MchResponse.SYSTEMERROR:
                // retry in 1 seconds later
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // skip
                }
                continue;
            default:
                // skip
                break;
            }
        }
        return result;
    }

    private TransferResponse postPayTransfer(WeChatAppProperties weChatApp, AuthToken token, String tradeNo, int amount,
            int retryCount) throws Exception {
        var params = new TransferParams();
        params.setMchAppid(weChatApp.getAppid());
        params.setMchid(weChatApp.getMchid());
        params.setNonceStr(Long.toString(Math.abs(RandomUtil.randomLong())));
        params.setPartnerTradeNo(tradeNo);
        params.setOpenid(token.getAccount().getOpenid());
        params.setAmount(amount);
        params.setDesc("恭喜发财");
        params.setSpbillCreateIp(token.getIp());
        params.signMd5(weChatApp.getMchSecret());
        log.debug("[WeChat] transfer params: {}", params);
        var body = XmlUtil.dumps(params);
        var uri = WECHAT_TRANSFERS_URI;
        var sendTime = System.currentTimeMillis();
        try {
            var resp = globalWechatMchHttpClient.request(uri).contentType(APPLICATION_XML)
                    .header(ACCEPT, APPLICATION_XML).post(HttpContentHolders.ofUtf8(body))
                    .sendAsync(HttpContentHandlers.ofString()).orTimeout(60, TimeUnit.SECONDS).get();
            var millis = System.currentTimeMillis() - sendTime;
            sdkWechatLogger.info("Post Transfer: {} >>> {} <<< {} {} ms - {}", uri, body, resp.status(), millis,
                    resp.content());
            if (resp.statusCode() != 200) {
                throw ApiErrors.partnerServiceError();
            }
            var content = resp.content();
            log.debug("[WeChat] post transfer response: {}", content);
            var result = XmlUtil.loads(content, TransferResponse.class);
            result.setBodyContent(content);
            return result;
        } catch (Exception e) {
            var millis = System.currentTimeMillis() - sendTime;
            sdkWechatLogger.error("Post Transfer Failed: {} <<< {} ms - {}", uri, millis, e);
            throw e;
        }
    }

}
