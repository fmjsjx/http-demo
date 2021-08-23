package com.github.fmjsjx.demo.http.server;

import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.fmjsjx.demo.http.core.model.AuthToken;
import com.github.fmjsjx.demo.http.service.TokenManager;
import com.github.fmjsjx.libnetty.http.server.HttpRequestContext;
import com.github.fmjsjx.libnetty.http.server.HttpResult;
import com.github.fmjsjx.libnetty.http.server.middleware.Middleware;
import com.github.fmjsjx.libnetty.http.server.middleware.MiddlewareChain;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;

@Component
public class TokenVerifier implements Middleware {

    private static final AsciiString X_TOKEN = AsciiString.cached("x-token");

    @Autowired
    private TokenManager tokenManager;

    @Override
    public CompletionStage<HttpResult> apply(HttpRequestContext ctx, MiddlewareChain next) {
        var xtoken = ctx.headers().get(X_TOKEN);
        if (xtoken == null) {
            return ctx.simpleRespond(HttpResponseStatus.FORBIDDEN);
        }
        return tokenManager.findTokenAsync(xtoken, ctx.eventLoop()).thenCompose(token -> {
            if (token.isPresent()) {
                ctx.property(AuthToken.KEY, token.get());
                return next.doNext(ctx);
            }
            return ctx.simpleRespond(HttpResponseStatus.FORBIDDEN);
        });
    }

}
