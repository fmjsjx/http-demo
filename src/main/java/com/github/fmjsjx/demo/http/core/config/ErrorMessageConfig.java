package com.github.fmjsjx.demo.http.core.config;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import lombok.ToString;

@ToString
public class ErrorMessageConfig {

    private static final AtomicReference<ErrorMessageConfig> INSTANCE_REF = new AtomicReference<>();

    public static final ErrorMessageConfig getInstance() {
        return INSTANCE_REF.get();
    }

    public static final ErrorMessageFactory getFactory(int code) {
        return getInstance().factory(code);
    }

    public static final ErrorMessageConfig set(ErrorMessageConfig config) {
        return INSTANCE_REF.getAndSet(config);
    }

    public static final ErrorMessageConfig loadFromYaml(InputStream in) {
        return new ErrorMessageConfig(Jackson2YamlLibrary.getInstance().loads(in));
    }

    final JsonNode config;
    @ToString.Exclude
    final IntObjectMap<ErrorMessageFactory> factories;

    private ErrorMessageConfig(JsonNode config) {
        this.config = config;
        var factories = new IntObjectHashMap<ErrorMessageFactory>(config.size() * 2);
        for (var iter = config.fields(); iter.hasNext();) {
            var e = iter.next();
            var code = Integer.parseInt(e.getKey());
            var message = e.getValue().asText();
            factories.put(code, new ErrorMessageFactoryImpl(message));
        }
        this.factories = factories;
    }

    public ErrorMessageFactory factory(int code) {
        var factory = factories.get(code);
        if (factory == null) {
            return OriginalErrorMessageFactory.INSTANCE;
        }
        return factory;
    }

    public interface ErrorMessageFactory {

        String message(String original, Object... params);
    }

    @ToString
    static final class ErrorMessageFactoryImpl implements ErrorMessageFactory {

        final String message;
        @ToString.Exclude
        final Function<Object[], String> factory;

        public ErrorMessageFactoryImpl(String message) {
            var m = message.intern();
            this.message = m;
            var split = m.split("\\{\\d+\\}");
            if (split.length == 1) {
                factory = p -> m;
            } else {
                factory = p -> {
                    var b = new StringBuilder();
                    var plen = p.length;
                    if (plen < split.length - 1) {
                        for (int i = 0; i < plen; i++) {
                            b.append(split[i]).append(p[i]);
                        }
                        for (int i = plen; i < split.length; i++) {
                            b.append(split[i]);
                        }
                    } else {
                        for (int i = 0; i < split.length - 1; i++) {
                            b.append(split[i]).append(p[i]);
                        }
                        b.append(split[split.length - 1]);
                    }
                    return b.toString();
                };
            }
        }

        @Override
        public String message(String original, Object... params) {
            return factory.apply(params);
        }

    }

    private static final class OriginalErrorMessageFactory implements ErrorMessageFactory {

        private static final OriginalErrorMessageFactory INSTANCE = new OriginalErrorMessageFactory();

        @Override
        public String message(String original, Object... params) {
            return original;
        }

    }

}
