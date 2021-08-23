package com.github.fmjsjx.demo.http.server;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.github.fmjsjx.demo.http.api.ApiErrors;
import com.github.fmjsjx.demo.http.exception.ApiErrorException;
import com.github.fmjsjx.libnetty.http.HttpCommonUtil;
import com.github.fmjsjx.libnetty.http.server.HttpRequestContext;
import com.github.fmjsjx.libnetty.http.server.HttpResult;
import com.github.fmjsjx.libnetty.http.server.component.ExceptionHandler;
import com.github.fmjsjx.libnetty.http.server.component.JsonLibrary;
import com.mongodb.MongoException;

import io.lettuce.core.RedisException;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RouteErrorHandler implements ExceptionHandler {

    private static final AsciiString APPLICATION_JSON_UTF8 = HttpCommonUtil
            .contentType(HttpHeaderValues.APPLICATION_JSON);

    @Override
    public Optional<CompletionStage<HttpResult>> handle(HttpRequestContext ctx, Throwable cause) {
        if (cause instanceof ApiErrorException) {
            return Optional.of(handle0(ctx, (ApiErrorException) cause));
        }
        if (cause instanceof DataAccessException || cause instanceof MongoException
                || cause instanceof RedisException) {
            return Optional.of(handle0(ctx, ApiErrors.dataAccessError(cause)));
        }
        return Optional.empty();
    }

    private static final CompletionStage<HttpResult> handle0(HttpRequestContext ctx, ApiErrorException cause) {
        var result = cause.toResult();
        log.debug("Handle {} ==> {}", cause, result);
        var content = ctx.component(JsonLibrary.class).get().write(ctx.alloc(), result);
        return ctx.simpleRespond(HttpResponseStatus.OK, content, APPLICATION_JSON_UTF8);
    }

}
